package com.gamelibrary2d.components.frames;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.disposal.Disposable;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.components.containers.AbstractLayer;
import com.gamelibrary2d.exceptions.InitializationException;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.updaters.Updater;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFrame extends AbstractLayer<Renderable> implements Frame {
    private final Deque<Runnable> invokeLater = new ArrayDeque<>();
    private final DefaultFrameInitializationContext initializationContext = new DefaultFrameInitializationContext();
    private final Deque<Updater> updaters = new ArrayDeque<>();
    private final DisposerStack disposerStack = new DisposerStack();

    private boolean paused;
    private boolean initialized;
    private volatile boolean loaded;
    private Color backgroundColor = Color.BLACK;

    public void invokeLater(Runnable runnable) {
        invokeLater.addLast(runnable);
    }

    @Override
    public void registerDisposal(Disposable disposable) {
        disposerStack.push(disposable);
    }

    @Override
    public void initialize(Disposer disposer) throws InitializationException {
        if (isInitialized()) {
            return;
        }

        if (disposer == this) {
            throw new InitializationException("Cannot register itself as disposer");
        }

        disposer.registerDisposal(this);

        DefaultFrameInitializationContext context = new DefaultFrameInitializationContext();
        try {
            onInitialize(context);
        } catch (IOException e) {
            throw new InitializationException(e);
        }
        this.initializationContext.registerAll(context);

        disposerStack.pushBreak();
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public final FrameInitializationContext load() throws InitializationException {
        if (isLoaded()) {
            throw new InitializationException("Frame has already been loaded");
        }

        if (!isInitialized()) {
            throw new InitializationException("Frame has not been initialized");
        }

        DefaultFrameInitializationContext context = new DefaultFrameInitializationContext(this.initializationContext);
        handleLoad(context);
        return context;
    }

    protected void handleLoad(FrameInitializationContext context) throws InitializationException {
        onLoad(context);
        loaded = true;
    }

    @Override
    public void loaded(FrameInitializationContext context) throws InitializationException {
        try {
            onLoaded(context);
        } catch (IOException e) {
            throw new InitializationException(e);
        }
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void dispose(FrameDisposal disposal) {
        switch (disposal) {
            case NONE:
                break;
            case UNLOAD:
                unload();
                break;
            case DISPOSE:
                dispose();
                break;
        }
    }

    protected void unload() {
        // Dispose all resources created after the initialization phase.
        disposerStack.disposeUntilBreak();
        commonCleanUp();
    }

    @Override
    public void dispose() {
        if (initialized) {
            disposerStack.dispose();
            initializationContext.clear();
            commonCleanUp();
            initialized = false;
        }
    }

    private void commonCleanUp() {
        clear();
        updaters.clear();
        invokeLater.clear();
        loaded = false;
    }

    @Override
    public void runUpdater(Updater updater, boolean reset) {
        if (!updaters.contains(updater))
            updaters.addLast(updater);
        if (reset) {
            updater.reset();
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (!isPaused()) {
            super.onUpdate(deltaTime);

            for (int i = 0; i < updaters.size(); ++i) {
                Updater updater = updaters.pollFirst();
                updater.update(deltaTime);
                if (!updater.isFinished()) {
                    updaters.addLast(updater);
                }
            }
        }

        while (!invokeLater.isEmpty()) {
            invokeLater.pollFirst().run();
        }
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    protected void setBackgroundColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Background color cannot be null");
        }

        this.backgroundColor = color;
    }

    @Override
    public void begin() {
        if (!isLoaded()) {
            throw new IllegalStateException("Frame has not been loaded");
        }

        onBegin();
    }

    @Override
    public void end() {
        onEnd();
        invokeLater.clear();
    }

    protected abstract void onInitialize(FrameInitializationContext context) throws IOException, InitializationException;

    protected abstract void onLoad(FrameInitializationContext context) throws InitializationException;

    protected abstract void onLoaded(FrameInitializationContext context) throws IOException, InitializationException;

    protected abstract void onBegin();

    protected abstract void onEnd();

    private static class DisposerStack {
        private final static Disposable breakMark = () -> {
        };

        private final Deque<Disposable> stack = new ArrayDeque<>();

        void push(Disposable disposable) {
            stack.addLast(disposable);
        }

        void pushBreak() {
            stack.addLast(breakMark);
        }

        void disposeUntilBreak() {
            while (!stack.isEmpty()) {
                Disposable e = stack.pollLast();
                if (e == breakMark) {
                    stack.addLast(e);
                    return;
                }
                e.dispose();
            }
        }

        void dispose() {
            while (!stack.isEmpty()) {
                stack.pollLast().dispose();
            }
        }
    }

    private static class DefaultFrameInitializationContext implements FrameInitializationContext {
        private final Map<Object, Object> register;

        public DefaultFrameInitializationContext() {
            register = new HashMap<>();
        }

        public DefaultFrameInitializationContext(DefaultFrameInitializationContext other) {
            register = new HashMap<>(other.register);
        }

        @Override
        public void register(Object key, Object obj) {
            register.put(key, obj);
        }

        @Override
        public <T> T get(Class<T> type, Object key) {
            Object obj = register.get(key);
            if (type.isAssignableFrom(obj.getClass())) {
                return type.cast(obj);
            }

            return null;
        }

        /**
         * Clears all registered objects.
         */
        public void clear() {
            register.clear();
        }

        /**
         * Registers all objects from the specified context.
         */
        public void registerAll(DefaultFrameInitializationContext other) {
            register.putAll(other.register);
        }
    }
}