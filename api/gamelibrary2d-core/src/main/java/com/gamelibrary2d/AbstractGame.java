package com.gamelibrary2d;

import com.gamelibrary2d.common.disposal.AbstractDisposer;
import com.gamelibrary2d.common.event.DefaultEventPublisher;
import com.gamelibrary2d.common.event.EventPublisher;
import com.gamelibrary2d.common.functional.Action;
import com.gamelibrary2d.common.io.Read;
import com.gamelibrary2d.components.denotations.InputAware;
import com.gamelibrary2d.components.denotations.KeyDownAware;
import com.gamelibrary2d.components.denotations.KeyUpAware;
import com.gamelibrary2d.components.frames.Frame;
import com.gamelibrary2d.framework.Runtime;
import com.gamelibrary2d.framework.*;
import com.gamelibrary2d.opengl.OpenGLState;
import com.gamelibrary2d.opengl.shaders.DefaultShader;
import com.gamelibrary2d.opengl.shaders.DefaultShaderProgram;
import com.gamelibrary2d.opengl.shaders.ShaderType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * An abstract implementation of the Game interface. This is a general
 * implementation and can be used as base class for all games.
 */
public abstract class AbstractGame extends AbstractDisposer implements Game {

    private final EventPublisher<Frame> frameChangedPublisher = new DefaultEventPublisher<>();
    private final DelayedActionMonitor delayedActionMonitor = new DelayedActionMonitor();

    /**
     * True whenever the game window has cursor focus, each index represents a pointer id.
     */
    private final boolean[] pointerFocus = new boolean[10];

    /**
     * The OpenGL window used for rendering.
     */
    private Window window;

    /**
     * True while inside an update cycle. Used to determine if some actions, such as
     * changing frame, can be done instantly or if it should be delayed until after
     * the current cycle.
     */
    private boolean updating;

    /**
     * The current frame.
     */
    private Frame frame;

    /**
     * True if the current frame has not yet been updated, used so that the
     * deltaTime can be set to 0 for the first update (to avoid a very big initial
     * delta time).
     */
    private boolean frameNotUpdated;

    /**
     * The game loop is responsible for maintaining a steady frame rate.
     */
    private GameLoop gameLoop;

    /**
     * Speed factor each update, applied to the delta-time
     */
    private float speedFactor = 1;

    protected AbstractGame(Framework framework) {
        Runtime.initialize(framework);
    }

    @Override
    public void start(Window window, GameLoop gameLoop) throws IOException {
        this.gameLoop = gameLoop;
        this.window = window;
        window.initialize();
        window.setEventListener(new InternalWindowEventListener());
        initializeOpenGLSettings();
        createDefaultShaderPrograms();
        gameLoop.initialize(this::update, this::dispose, window);
        onStart();
        window.show();
        gameLoop.start(this::onExit);
    }

    private void initializeOpenGLSettings() {
        OpenGL.instance().glDisable(OpenGL.GL_DEPTH_TEST);
        // OpenGL.instance().glEnable(OpenGL.GL_CULL_FACE);
        // OpenGL.instance().glCullFace(OpenGL.GL_BACK);
    }

    private DefaultShader loadShader(String path, ShaderType shaderType) {
        try (InputStream stream = DefaultShader.class.getClassLoader().getResourceAsStream(path)) {
            String src = Read.text(stream, StandardCharsets.UTF_8);
            return DefaultShader.create(src, shaderType, this);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to load a shader file!"
                    + System.lineSeparator() + ex.getMessage());
        }
    }

    private void createComputeShaderPrograms() {
        DefaultShaderProgram particleUpdaterProgram = DefaultShaderProgram.create(this);
        particleUpdaterProgram.attachShader(loadShader("shaders/ParticleUpdater.compute", ShaderType.COMPUTE));
        particleUpdaterProgram.initialize();
        OpenGLState.setPrimaryParticleUpdaterProgram(particleUpdaterProgram);
    }

    private void createGeometryShaderPrograms() {
        DefaultShaderProgram quadParticleShaderProgram = DefaultShaderProgram.create(this);
        quadParticleShaderProgram.attachShader(loadShader("shaders/QuadParticle.geometry", ShaderType.GEOMETRY));
        quadParticleShaderProgram.attachShader(loadShader("shaders/QuadParticle.vertex", ShaderType.VERTEX));
        quadParticleShaderProgram.attachShader(loadShader("shaders/QuadParticle.fragment", ShaderType.FRAGMENT));
        quadParticleShaderProgram.initialize();
        quadParticleShaderProgram.initializeMvp(window.getWidth(), window.getHeight());
        OpenGLState.setQuadParticleShaderProgram(quadParticleShaderProgram);

        DefaultShaderProgram quadShaderProgram = DefaultShaderProgram.create(this);
        quadShaderProgram.attachShader(loadShader("shaders/Quad.geometry", ShaderType.GEOMETRY));
        quadShaderProgram.attachShader(loadShader("shaders/Quad.vertex", ShaderType.VERTEX));
        quadShaderProgram.attachShader(loadShader("shaders/Quad.fragment", ShaderType.FRAGMENT));
        quadShaderProgram.initialize();
        quadShaderProgram.initializeMvp(window.getWidth(), window.getHeight());
        OpenGLState.setQuadShaderProgram(quadShaderProgram);
    }

    private void createVersionSpecificShaderPrograms() {
        OpenGL.OpenGLVersion supportedVersion = Runtime.getFramework().getOpenGL().getSupportedVersion();
        switch (supportedVersion) {
            case OPENGL_ES_3:
                break;
            case OPENGL_ES_3_1:
                try {
                    createGeometryShaderPrograms();
                } catch (Exception e) {
                    System.err.println("Failed to create one or more shader programs. The device might not support the OpenGL ES 3.1 geometry shader extension.");
                    e.printStackTrace();
                }
            case OPENGL_ES_3_2:
                createGeometryShaderPrograms();
                break;
            case OPENGL_CORE_430:
                createGeometryShaderPrograms();
                createComputeShaderPrograms();
                break;
        }
    }

    private void createDefaultShaderPrograms() {
        DefaultShaderProgram defaultShaderProgram = DefaultShaderProgram.create(this);
        defaultShaderProgram.attachShader(loadShader("shaders/Default.vertex", ShaderType.VERTEX));
        defaultShaderProgram.attachShader(loadShader("shaders/Default.fragment", ShaderType.FRAGMENT));
        defaultShaderProgram.initialize();
        defaultShaderProgram.initializeMvp(window.getWidth(), window.getHeight());
        OpenGLState.setPrimaryShaderProgram(defaultShaderProgram);

        DefaultShaderProgram pointParticleShaderProgram = DefaultShaderProgram.create(this);
        pointParticleShaderProgram
                .attachShader(loadShader("shaders/PointParticle.vertex", ShaderType.VERTEX));
        pointParticleShaderProgram
                .attachShader(loadShader("shaders/PointParticle.fragment", ShaderType.FRAGMENT));
        pointParticleShaderProgram.initialize();
        pointParticleShaderProgram.initializeMvp(window.getWidth(), window.getHeight());
        OpenGLState.setPointParticleShaderProgram(pointParticleShaderProgram);

        DefaultShaderProgram pointShaderProgram = DefaultShaderProgram.create(this);
        pointShaderProgram
                .attachShader(loadShader("shaders/Point.vertex", ShaderType.VERTEX));
        pointShaderProgram
                .attachShader(loadShader("shaders/Point.fragment", ShaderType.FRAGMENT));
        pointShaderProgram.initialize();
        pointShaderProgram.initializeMvp(window.getWidth(), window.getHeight());
        OpenGLState.setPointShaderProgram(pointShaderProgram);

        createVersionSpecificShaderPrograms();
    }

    @Override
    public void setViewPort(int x, int y, int width, int height) {
        OpenGL.instance().glViewport(x, y, width, height);
    }

    @Override
    public boolean hasPointerFocus(int id) {
        return id < pointerFocus.length && pointerFocus[id];
    }

    @Override
    protected void onDispose() {
        window.dispose();
    }

    @Override
    public void exit() {
        gameLoop.stop();
    }

    protected float getSpeedFactor() {
        return speedFactor;
    }

    protected void setSpeedFactor(float speedFactor) {
        this.speedFactor = speedFactor;
    }

    public void update(float deltaTime) {
        delayedActionMonitor.run();

        try {
            updating = true;
            window.pollEvents();
            Frame currentFrame = frame;
            update(currentFrame, deltaTime * speedFactor);
            render(currentFrame);
        } finally {
            updating = false;
        }
    }

    private void update(Frame frame, float deltaTime) {
        if (frame != null) {
            frame.update(frameNotUpdated ? 0 : deltaTime);
            frameNotUpdated = false;
        }
    }

    @Override
    public void render() {
        render(frame);
    }

    private void render(Frame frame) {
        window.render(frame.getBackgroundColor(), frame, 1.0f);
    }

    @Override
    public void setFrame(Frame frame, boolean disposePrevious) {
        if (updating) {
            invokeLater(() -> beginFrame(frame, disposePrevious));
        } else {
            beginFrame(frame, disposePrevious);
        }
    }

    private void beginFrame(Frame frame, boolean disposePrevious) {
        Frame previousFrame = this.frame;
        if (previousFrame != null) {
            previousFrame.end();
            if (disposePrevious) {
                previousFrame.dispose();
            }
        }

        this.frame = frame;
        frameNotUpdated = true;
        if (frame != null) {
            frameChangedPublisher.publish(frame);
        }

        if (frame != null) {
            frame.begin();
        }
    }

    @Override
    public void addFrameChangedListener(FrameChangedListener listener) {
        frameChangedPublisher.addListener(listener);
    }

    @Override
    public void removeFrameChangedListener(FrameChangedListener listener) {
        frameChangedPublisher.removeListener(listener);
    }

    @Override
    public Frame getFrame() {
        return frame;
    }

    public void setFrame(Frame frame) {
        setFrame(frame, false);
    }

    @Override
    public Window getWindow() {
        return window;
    }

    @Override
    public void invokeLater(Action action) {
        delayedActionMonitor.add(action);
    }

    protected abstract void onStart() throws IOException;

    protected abstract void onExit();

    private static class DelayedActionMonitor {
        private final Deque<Action> actions = new ArrayDeque<>();

        synchronized void add(Action action) {
            actions.add(action);
        }

        synchronized void run() {
            int size = actions.size();
            for (int i = 0; i < size; ++i) {
                actions.pollFirst().perform();
            }
        }
    }

    private class InternalWindowEventListener implements WindowEventListener {

        @Override
        public void onKeyAction(int key, KeyAction action) {
            Frame frame = getFrame();
            switch (action) {
                case DOWN:
                    if (frame instanceof KeyDownAware) {
                        ((KeyDownAware) frame).keyDown(key, false);
                    }
                    FocusManager.keyDownEvent(key, false);
                    break;
                case DOWN_REPEAT:
                    if (frame instanceof KeyDownAware) {
                        ((KeyDownAware) frame).keyDown(key, true);
                    }
                    FocusManager.keyDownEvent(key, true);
                    break;
                case UP:
                    if (frame instanceof KeyUpAware) {
                        ((KeyUpAware) frame).keyUp(key);
                    }
                    FocusManager.keyUpEvent(key);
                    break;
            }
        }

        @Override
        public void onCharInput(char charInput) {
            if (frame instanceof InputAware)
                ((InputAware) frame).charInput(charInput);
            FocusManager.charInputEvent(charInput);
        }

        @Override
        public void onPointerMove(int id, float posX, float posY) {
            try {
                FocusManager.onPointerActive();
                Frame frame = getFrame();
                if (frame != null) {
                    frame.pointerMove(id, posX, posY, posX, posY);
                }
            } finally {
                FocusManager.onPointerInactive();
            }
        }

        @Override
        public void onPointerEnter(int id) {
            if (id < pointerFocus.length) {
                pointerFocus[id] = true;
            }
        }

        @Override
        public void onPointerLeave(int id) {
            if (id < pointerFocus.length) {
                pointerFocus[id] = false;
            }
        }

        @Override
        public void onPointerAction(int id, int button, float posX, float posY, PointerAction action) {
            Frame frame = getFrame();
            if (frame != null) {
                try {
                    FocusManager.onPointerActive();
                    switch (action) {
                        case DOWN:
                            frame.pointerDown(id, button, posX, posY, posX, posY);
                            FocusManager.pointerDownFinished(id, button);
                            break;
                        case UP:
                            frame.pointerUp(id, button, posX, posY, posX, posY);
                            FocusManager.pointerUpFinished(id, button);
                            break;
                    }
                } finally {
                    FocusManager.onPointerInactive();
                }
            }
        }

        @Override
        public void onScroll(int id, float xOffset, float yOffset) {

        }
    }
}
