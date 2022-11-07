package com.gamelibrary2d.components.frames;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.disposal.DefaultDisposer;
import com.gamelibrary2d.common.disposal.Disposable;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.common.functional.Action;
import com.gamelibrary2d.components.containers.AbstractLayer;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.updates.Update;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class AbstractFrame extends AbstractLayer<Renderable> implements Frame {
    private final DelayedActionMonitor delayedActionMonitor = new DelayedActionMonitor();
    private final Deque<Update> updates = new ArrayDeque<>();
    private final DefaultDisposer disposer;
    private Color backgroundColor = Color.BLACK;
    private boolean initialized;
    private Future<FrameInitializationContext> initializationContextFuture;

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
        clearDelayedActions();
        initialize();
    }

    /**
     * Removes all actions that have been queued by {@link #invokeLater}.
     */
    protected void clearDelayedActions() {
        delayedActionMonitor.clear();
    }

    /**
     * This method sets {@link #isInitialized} to false, invokes {@link #onInitialize(FrameInitializer)} and runs the initialization pipeline.
     * Note that no cleanup is attempted. It's up to the invoker to set the frame in a state so that it can be re-initialized.
     */
    protected void reInitialize() {
        abortInitialization();
        initialize();
    }

    private void initialize() {
        initialized = false;

        FrameInitializer frameInitializer = new FrameInitializer(this);

        try {
            onInitialize(frameInitializer);
            initializationContextFuture = frameInitializer.run();
        } catch (Throwable e) {
            CompletableFuture<FrameInitializationContext> completedFuture = new CompletableFuture<>();
            completedFuture.completeExceptionally(e);
            initializationContextFuture = completedFuture;
        }

        tryCompleteInitialization();
    }

    private void tryCompleteInitialization() {
        initialized = initializationContextFuture.isDone();

        if (initialized) {
            try {
                onInitializationSuccessful(initializationContextFuture.get());
            } catch (InterruptedException | ExecutionException e) {
                onInitializationFailed(e);
            } finally {
                initializationContextFuture = null;
            }
        }
    }

    @Override
    public void end() {
        onEnd();
        clearDelayedActions();
    }

    @Override
    public void dispose() {
        abortInitialization();
        initialized = false;

        clear();

        disposer.dispose();
        disposer.clear();
        updates.clear();
        clearDelayedActions();

        onDispose();
    }

    private void abortInitialization() {
        if (initializationContextFuture != null) {
            initializationContextFuture.cancel(true);
            initializationContextFuture = null;
        }
    }

    @Override
    public void startUpdate(Update update) {
        if (!updates.contains(update)) {
            updates.addLast(update);
        }
    }

    @Override
    public void stopUpdate(Update update) {
        updates.remove(update);
    }

    protected boolean isInitialized() {
        return initialized;
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

        for (int i = 0; i < updates.size(); ++i) {
            Update update = updates.pollFirst();
            update.update(deltaTime);
            if (!update.isFinished()) {
                updates.addLast(update);
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

    /**
     * Invoked when the frame begins or if {@link #reInitialize} is invoked.
     *
     * @param initializer Used to configure the frame initialization pipeline.
     *                    Either {@link #onInitializationSuccessful} or {@link #onInitializationFailed} will be invoked
     *                    when the pipeline has finished.
     * @throws IOException Throwing this exception will result in {@link #onInitializationFailed} being invoked.
     */
    protected abstract void onInitialize(FrameInitializer initializer) throws IOException;

    /**
     * Invoked if a task of the {@link FrameInitializer} throws an exception.
     * This method is always invoked from the main thread, even if the task ran as a background task.
     *
     * @param error The initialization exception.
     */
    protected abstract void onInitializationFailed(Throwable error);

    /**
     * Invoked when all tasks of the {@link FrameInitializer} has completed successfully,
     * or directly after {@link #onInitialize} if no {@link FrameInitializationTask initialization tasks} were added.
     *
     * @param context The context from the initialization pipeline.
     */
    protected abstract void onInitializationSuccessful(FrameInitializationContext context);

    /**
     * Invoked when the frame ends.
     */
    protected abstract void onEnd();

    /**
     * Invoked when the frame is disposed.
     */
    protected abstract void onDispose();

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

        synchronized void clear() {
            actions.clear();
        }
    }
}