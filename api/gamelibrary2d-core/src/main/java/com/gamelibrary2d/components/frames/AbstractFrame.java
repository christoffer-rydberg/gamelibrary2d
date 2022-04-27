package com.gamelibrary2d.components.frames;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.disposal.DefaultDisposer;
import com.gamelibrary2d.common.disposal.Disposable;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.common.functional.Action;
import com.gamelibrary2d.components.containers.AbstractLayer;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.updaters.Updater;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class AbstractFrame extends AbstractLayer<Renderable> implements Frame {
    private final Deque<Action> invokeLater = new ArrayDeque<>();
    private final Deque<Updater> updaters = new ArrayDeque<>();
    private final DefaultDisposer disposer;

    private boolean initialized;
    private boolean requiresInitialization = true;
    private Color backgroundColor = Color.BLACK;
    private CompletableFuture<FrameInitializationContext> initializationContextFuture;

    @Override
    public void invokeLater(Action runnable) {
        invokeLater.addLast(runnable);
    }

    @Override
    public void registerDisposal(Disposable disposable) {
        disposer.registerDisposal(disposable);
    }

    protected AbstractFrame(Disposer parentDisposer) {
        this.disposer = new DefaultDisposer(parentDisposer);
    }

    @Override
    public void begin() {
        if (requiresInitialization) {
            requiresInitialization = false;

            DefaultFrameInitializer frameInitializer = new DefaultFrameInitializer(this);

            try {
                initialize(frameInitializer);
                initializationContextFuture = frameInitializer.run();
            } catch (Throwable e) {
                initializationContextFuture = new CompletableFuture<>();
                initializationContextFuture.completeExceptionally(e);
            }

            if (initializationContextFuture != null) {
                tryCompleteInitialization();
            }
        }

        onBegin();
    }

    @Override
    public void end() {
        onEnd();
        invokeLater.clear();
    }

    protected void initialize(FrameInitializer initializer) throws Throwable {
        onInitialize(initializer);
    }

    @Override
    public void dispose() {
        if (initializationContextFuture != null) {
            initializationContextFuture.cancel(true);
            initializationContextFuture = null;
        }

        clear();

        disposer.dispose();
        disposer.clear();
        updaters.clear();
        invokeLater.clear();
        initialized = false;
        requiresInitialization = true;

        onDispose();
    }

    @Override
    public void startUpdater(Updater updater) {
        if (!updaters.contains(updater)) {
            updaters.addLast(updater);
        }
    }

    @Override
    public void stopUpdater(Updater updater) {
        updaters.remove(updater);
    }

    private void tryCompleteInitialization() {
        if (initializationContextFuture.isDone()) {
            try {
                onInitialized(initializationContextFuture.get(), null);
                initialized = true;
            } catch (InterruptedException e) {
                requiresInitialization = true;
                onInitialized(null, e);
            } catch (ExecutionException e) {
                requiresInitialization = true;
                onInitialized(null, e.getCause());
            } finally {
                initializationContextFuture = null;
            }
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        while (!invokeLater.isEmpty()) {
            invokeLater.pollFirst().perform();
        }

        if (initializationContextFuture != null) {
            tryCompleteInitialization();
        }

        super.onUpdate(deltaTime);

        for (int i = 0; i < updaters.size(); ++i) {
            Updater updater = updaters.pollFirst();
            updater.update(deltaTime);
            if (!updater.isFinished()) {
                updaters.addLast(updater);
            }
        }
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

    protected boolean isInitialized() {
        return initialized;
    }

    protected abstract void onInitialize(FrameInitializer initializer) throws Throwable;

    protected abstract void onInitialized(FrameInitializationContext context, Throwable error);

    protected abstract void onBegin();

    protected abstract void onEnd();

    protected abstract void onDispose();
}