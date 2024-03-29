package com.gamelibrary2d;

import com.gamelibrary2d.components.denotations.InputAware;
import com.gamelibrary2d.components.denotations.KeyDownAware;
import com.gamelibrary2d.components.denotations.KeyUpAware;
import com.gamelibrary2d.components.frames.Frame;
import com.gamelibrary2d.disposal.AbstractDisposer;
import com.gamelibrary2d.event.DefaultEventPublisher;
import com.gamelibrary2d.event.EventPublisher;
import com.gamelibrary2d.functional.Action;
import com.gamelibrary2d.input.KeyAction;
import com.gamelibrary2d.input.PointerAction;
import com.gamelibrary2d.io.Read;
import com.gamelibrary2d.opengl.OpenGLState;
import com.gamelibrary2d.opengl.shaders.DefaultShader;
import com.gamelibrary2d.opengl.shaders.DefaultShaderProgram;
import com.gamelibrary2d.opengl.shaders.ShaderType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * An abstract implementation of the Game interface. This is a general
 * implementation and can be used as base class for all games.
 */
public abstract class AbstractGame extends AbstractDisposer implements Game {
    private final GameKeyAndPointerState inputState = new GameKeyAndPointerState();

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
        gameLoop.initialize(this, this::dispose, window);
        onStart();
        window.show();
        gameLoop.start(this::onExit);
    }

    private void initializeOpenGLSettings() {
        OpenGL.instance().glDisable(OpenGL.GL_DEPTH_TEST);
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
            if (currentFrame != null) {
                update(currentFrame, deltaTime * speedFactor);
                render(currentFrame);
            }
        } finally {
            updating = false;
        }
    }

    private void update(Frame frame, float deltaTime) {
        frame.update(frameNotUpdated ? 0 : deltaTime);
        frameNotUpdated = false;
    }

    @Override
    public void render() {
        if (frame != null) {
            render(frame);
        }
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
                    inputState.setKeyDown(key);
                    if (frame instanceof KeyDownAware) {
                        ((KeyDownAware) frame).keyDown(inputState, key, false);
                    }
                    FocusManager.keyDownEvent(inputState, key, false);
                    break;
                case DOWN_REPEAT:
                    if (frame instanceof KeyDownAware) {
                        ((KeyDownAware) frame).keyDown(inputState, key, true);
                    }
                    FocusManager.keyDownEvent(inputState, key, true);
                    break;
                case UP:
                    inputState.setKeyUp(key);
                    if (frame instanceof KeyUpAware) {
                        ((KeyUpAware) frame).keyUp(inputState, key);
                    }
                    FocusManager.keyUpEvent(inputState, key);
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
                    frame.pointerMove(inputState, id, posX, posY);
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
                            inputState.setPointerDown(id, button);
                            frame.pointerDown(inputState, id, button, posX, posY);
                            FocusManager.pointerDownFinished(inputState, id, button);
                            break;
                        case UP:
                            inputState.setPointerUp(id, button);
                            frame.pointerUp(inputState, id, button, posX, posY);
                            FocusManager.pointerUpFinished(inputState, id, button);

                            // Faking a pointer move after a pointer up is useful to restore e.g. hovering state.
                            onPointerMove(id, posX, posY);
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

    private static class GameKeyAndPointerState implements KeyAndPointerState {
        private final ButtonState keyState = new ButtonState();
        private final Map<Integer, ButtonState> pointerState = new HashMap<>();

        public boolean isPointerDown(int pointerId) {
            ButtonState buttonState = pointerState.get(pointerId);
            return buttonState != null && buttonState.isDown();
        }

        public boolean isPointerDown(int pointerId, int button) {
            ButtonState buttonState = pointerState.get(pointerId);
            return buttonState != null && buttonState.isDown(button);
        }

        @Override
        public boolean isKeyDown(int key) {
            return keyState.isDown(key);
        }

        void setKeyDown(int key) {
            keyState.setDown(key);
        }

        void setKeyUp(int key) {
            keyState.setUp(key);
        }

        void setPointerDown(int pointerId, int button) {
            ButtonState buttonState = pointerState.get(pointerId);
            if (buttonState == null) {
                buttonState = new ButtonState();
                pointerState.put(pointerId, buttonState);
            }

            buttonState.setDown(button);
        }

        void setPointerUp(int pointerId, int button) {
            ButtonState buttonState = pointerState.get(pointerId);
            if (buttonState != null) {
                buttonState.setUp(button);
            }
        }

        private static class ButtonState {
            private final Set<Integer> state = new HashSet<>();

            public boolean isDown() {
                return !state.isEmpty();
            }

            public boolean isDown(int button) {
                return state.contains(button);
            }

            public void setDown(int button) {
                state.add(button);
            }

            public void setUp(int button) {
                state.remove(button);
            }
        }
    }
}
