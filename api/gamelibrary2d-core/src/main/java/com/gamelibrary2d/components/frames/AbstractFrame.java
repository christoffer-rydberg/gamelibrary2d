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
    private final DelayedActionMonitor delayedActionMonitor = new DelayedActionMonitor();
    private final Deque<Updater> updaters = new ArrayDeque<>();
    private final DefaultDisposer disposer;

    private boolean initialized;
    private boolean requiresInitialization = true;
    private Color backgroundColor = Color.BLACK;
    private CompletableFuture<FrameInitializationContext> initializationContextFuture;

    protected AbstractFrame(Disposer parentDisposer) {
        this.disposer = new DefaultDisposer(parentDisposer);
    }

    @Override
    public void invokeLater(Action action) {
        delayedActionMonitor.add(action);
    }

    @Override
    public void registerDisposal(Disposable disposable) {
        disposer.registerDisposal(disposable);
    }

    @Override
    public void begin() {
        if (requiresInitialization) {
            requiresInitialization = false;

            delayedActionMonitor.clear();

            FrameInitializer frameInitializer = new FrameInitializer(this);

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
        delayedActionMonitor.clear();
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
        delayedActionMonitor.clear();
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
    protected final void handleUpdate(float deltaTime) {
        delayedActionMonitor.run();

        if (initializationContextFuture != null) {
            tryCompleteInitialization();
        }

        onUpdate(deltaTime);
    }

    protected void onUpdate(float deltaTime) {
        super.handleUpdate(deltaTime);

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

    protected abstract void initialize(FrameInitializer initializer) throws Throwable;

    /**
     * Invoked when all initialization tasks of the {@link FrameInitializer} has completed.
     *
     * @param context The context from the initialization pipeline.
     * @param error   The exception, in case of failed initialization, otherwise null.
     */
    protected abstract void onInitialized(FrameInitializationContext context, Throwable error);

    protected abstract void onBegin();

    protected abstract void onEnd();

    protected abstract void onDispose();

    private static class DelayedActionMonitor {
        private final Deque<Action> actions = new ArrayDeque<>();

        synchronized void add(Action action) {
            actions.add(action);
        }

        synchronized void run() {
            while (!actions.isEmpty()) {
                actions.pollFirst().perform();
            }
        }

        synchronized void clear() {
            actions.clear();
        }
    }
}