package com.gamelibrary2d.components.frames;

import java.util.concurrent.*;

public class Pipeline {
    private final Frame frame;
    private final ConcurrentLinkedQueue<PipelineTaskWrapper> tasks = new ConcurrentLinkedQueue<>();
    private final PipelineContext context;
    private final PipelineFuture pipelineFuture = new PipelineFuture();

    Pipeline(Frame frame, PipelineContext context) {
        this.frame = frame;
        this.context = context;
    }

    private void performTask(PipelineTask task) {
        if (!pipelineFuture.isDone()) {
            try {
                task.perform(context);
            } catch (Throwable e) {
                pipelineFuture.completeExceptionally(e);
            }
        }
    }

    private void performTask(PipelineTaskWrapper task) {
        if (task.isBackgroundTask) {
            pipelineFuture.runAsync(() -> {
                performTask(task.task);
                performNextTask();
            });
        } else {
            performTask(task.task);
            performNextTask();
        }
    }

    private void performNextTask() {
        PipelineTaskWrapper task = tasks.poll();
        if (task != null) {
            frame.invokeLater(() -> performTask(task));
        } else {
            pipelineFuture.complete();
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
        tasks.add(new PipelineTaskWrapper(task, false));
    }

    /**
     * Adds a task to the pipeline that will run in the background.
     * <br><br>The task will not block the update cycle when it runs, but since it's not running on the main thread
     * it won't have access to the OpenGL context.
     *
     * @param task The task
     */
    public void addBackgroundTask(PipelineTask task) {
        tasks.add(new PipelineTaskWrapper(task, true));
    }

    Future<Void> run() {
        performNextTask();
        return pipelineFuture;
    }

    private static class PipelineTaskWrapper {
        PipelineTask task;
        boolean isBackgroundTask;

        PipelineTaskWrapper(PipelineTask task, boolean isBackgroundTask) {
            this.task = task;
            this.isBackgroundTask = isBackgroundTask;
        }
    }

    private static class PipelineFuture implements Future<Void> {
        private Thread activeThread;
        private volatile boolean isCancelled;
        private volatile boolean isDone;
        private volatile Throwable error;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (isDone) {
                return false;
            }

            try {
                cancelActiveThread(mayInterruptIfRunning);
                return true;
            } finally {
                isDone = true;
                isCancelled = true;
            }
        }

        private void cancelActiveThread(boolean mayInterruptIfRunning) {
            if (activeThread != null) {
                try {
                    if (mayInterruptIfRunning) {
                        activeThread.interrupt();
                    }
                    activeThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    activeThread = null;
                }
            }
        }

        @Override
        public boolean isCancelled() {
            return isCancelled;
        }

        @Override
        public boolean isDone() {
            return isDone;
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            if (error != null) {
                throw new ExecutionException(error);
            }

            try {
                activeThread.join();
            } finally {
                isDone = true;
            }

            return null;
        }

        @Override
        public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            throw new RuntimeException("Not implemented");
        }

        public void completeExceptionally(Throwable e) {
            this.error = e;
            isDone = true;
        }

        public void complete() {
            isDone = true;
        }

        public void runAsync(Runnable runnable) {
            activeThread = new Thread(runnable);
            activeThread.start();
        }
    }
}
