package com.gamelibrary2d.components.frames;

import java.util.concurrent.*;

/**
 * Used to configure the frame initialization pipeline by adding {@link FrameInitializationTask initialization tasks}.
 * <br><br>
 * <p>
 * The initialization pipeline works as follows:
 * <br>- Each task runs sequentially in the order it was added.
 * <br>- Each update cycle will run at the most one task.
 * <br>- When a task has completed it will schedule the next task by invoking {@link Frame#invokeLater}
 */
public class FrameInitializer {
    private final Frame frame;
    private final ConcurrentLinkedQueue<InitializationTaskWrapper> tasks = new ConcurrentLinkedQueue<>();
    private final FrameInitializationContext context = new FrameInitializationContext();
    private final InitializationFuture initializationFuture = new InitializationFuture();

    FrameInitializer(Frame frame) {
        this.frame = frame;
    }

    private void performTask(FrameInitializationTask task) {
        if (!initializationFuture.isDone()) {
            try {
                task.perform(context);
            } catch (Throwable e) {
                initializationFuture.completeExceptionally(e);
            }
        }
    }

    private void performTask(InitializationTaskWrapper task) {
        if (task.isBackgroundTask) {
            initializationFuture.runAsync(() -> {
                performTask(task.task);
                performNextTask();
            });
        } else {
            performTask(task.task);
            performNextTask();
        }
    }

    private void performNextTask() {
        InitializationTaskWrapper task = tasks.poll();
        if (task != null) {
            frame.invokeLater(() -> performTask(task));
        } else {
            initializationFuture.complete(context);
        }
    }

    /**
     * Adds an initialization task to the pipeline.
     * <p>
     * The task will block the update cycle when it runs.
     *
     * @param task The initialization task
     */
    public void addTask(FrameInitializationTask task) {
        tasks.add(new InitializationTaskWrapper(task, false));
    }

    /**
     * Adds an initialization task to the pipeline that will run in the background.
     * <br><br>The task will not block the update cycle when it runs, but since it's not running on the main thread
     * it won't have access to the OpenGL context.
     * <br><br>Background tasks are generally safe to have side effects.
     * The underlying {@link CompletableFuture} assures that the main thread will have visibility of changed fields when
     * the task has completed. Fields that are accessed or modified in parallel with the task must be properly synchronized.
     *
     * @param task The initialization task
     */
    public void addBackgroundTask(FrameInitializationTask task) {
        tasks.add(new InitializationTaskWrapper(task, true));
    }

    Future<FrameInitializationContext> run() {
        frame.invokeLater(this::performNextTask);
        return initializationFuture;
    }

    private static class InitializationTaskWrapper {
        FrameInitializationTask task;
        boolean isBackgroundTask;

        InitializationTaskWrapper(FrameInitializationTask task, boolean isBackgroundTask) {
            this.task = task;
            this.isBackgroundTask = isBackgroundTask;
        }
    }

    private static class InitializationFuture implements Future<FrameInitializationContext> {
        private final CompletableFuture<FrameInitializationContext> completableFuture = new CompletableFuture<>();
        private CompletableFuture<Void> activeFuture;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (!cancelActiveFuture(mayInterruptIfRunning)) {
                return false;
            }

            return completableFuture.cancel(mayInterruptIfRunning);
        }

        private boolean cancelActiveFuture(boolean mayInterruptIfRunning) {
            if (activeFuture == null) {
                return true;
            }

            if (activeFuture.cancel(mayInterruptIfRunning)) {
                activeFuture = null;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean isCancelled() {
            return completableFuture.isCancelled();
        }

        @Override
        public boolean isDone() {
            return completableFuture.isDone();
        }

        @Override
        public FrameInitializationContext get() throws InterruptedException, ExecutionException {
            return completableFuture.get();
        }

        @Override
        public FrameInitializationContext get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return completableFuture.get(timeout, unit);
        }

        public void completeExceptionally(Throwable e) {
            completableFuture.completeExceptionally(e);
        }

        public void complete(FrameInitializationContext context) {
            completableFuture.complete(context);
        }

        public void runAsync(Runnable runnable) {
            activeFuture = CompletableFuture.runAsync(runnable);
        }
    }
}
