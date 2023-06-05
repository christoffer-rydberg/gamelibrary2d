package com.gamelibrary2d.components.frames;

import java.util.concurrent.*;

public class Pipeline {
    private final Frame frame;
    private final ConcurrentLinkedQueue<InitializationTaskWrapper> tasks = new ConcurrentLinkedQueue<>();
    private final PipelineContext context;
    private final InitializationFuture initializationFuture = new InitializationFuture();

    Pipeline(Frame frame, PipelineContext context) {
        this.frame = frame;
        this.context = context;
    }

    private void performTask(PipelineTask task) {
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
            initializationFuture.complete();
        }
    }

    /**
     * Adds a task to the pipeline.
     * <p>
     * The task will block the update cycle when it runs.
     *
     * @param task The task
     */
    public void addTask(PipelineTask task) {
        tasks.add(new InitializationTaskWrapper(task, false));
    }

    /**
     * Adds a task to the pipeline that will run in the background.
     * <br><br>The task will not block the update cycle when it runs, but since it's not running on the main thread
     * it won't have access to the OpenGL context.
     * <br><br>Background tasks are generally safe to have side effects.
     * The underlying {@link CompletableFuture} assures that the main thread will have visibility of changed fields when
     * the task has completed. Fields that are accessed or modified in parallel with the task must be properly synchronized.
     *
     * @param task The task
     */
    public void addBackgroundTask(PipelineTask task) {
        tasks.add(new InitializationTaskWrapper(task, true));
    }

    Future<Void> run() {
        performNextTask();
        return initializationFuture;
    }

    private static class InitializationTaskWrapper {
        PipelineTask task;
        boolean isBackgroundTask;

        InitializationTaskWrapper(PipelineTask task, boolean isBackgroundTask) {
            this.task = task;
            this.isBackgroundTask = isBackgroundTask;
        }
    }

    private static class InitializationFuture implements Future<Void> {
        private final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
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
        public Void get() throws InterruptedException, ExecutionException {
            return completableFuture.get();
        }

        @Override
        public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return completableFuture.get(timeout, unit);
        }

        public void completeExceptionally(Throwable e) {
            completableFuture.completeExceptionally(e);
        }

        public void complete() {
            completableFuture.complete(null);
        }

        public void runAsync(Runnable runnable) {
            activeFuture = CompletableFuture.runAsync(runnable);
        }
    }
}
