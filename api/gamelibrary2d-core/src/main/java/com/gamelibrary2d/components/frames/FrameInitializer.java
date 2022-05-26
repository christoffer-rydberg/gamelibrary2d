package com.gamelibrary2d.components.frames;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Used to configure the frame initialization pipeline by adding {@link InitializationTask initialization tasks}.
 * <br><br>
 * <p>
 * The initialization pipeline works as follows:
 * <br>- Each task runs sequentially in the order it was added.
 * <br>- Each update cycle will run at the most one task.
 * <br>- When a task has completed it will schedule the next task by invoking {@link Frame#invokeLater}
 */
public class FrameInitializer {
    private final Frame frame;
    private final CompletableFuture<FrameInitializationContext> completableFuture = new CompletableFuture<>();
    private final ConcurrentLinkedQueue<InitializationTaskWrapper> tasks = new ConcurrentLinkedQueue<>();
    private final FrameInitializationContext context = new FrameInitializationContext();

    FrameInitializer(Frame frame) {
        this.frame = frame;
    }

    private void performTask(InitializationTask task) {
        if (!completableFuture.isDone()) {
            try {
                task.perform(context);
            } catch (Throwable e) {
                completableFuture.completeExceptionally(e);
            }
        }
    }

    private void performTask(InitializationTaskWrapper task) {
        if (task.isBackgroundTask) {
            CompletableFuture.runAsync(() -> {
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
            completableFuture.complete(context);
        }
    }

    /**
     * Adds an initialization task to the pipeline.
     * <p>
     * The task will block the update cycle when it runs.
     *
     * @param task The initialization task
     */
    public void addTask(InitializationTask task) {
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
    public void addBackgroundTask(InitializationTask task) {
        tasks.add(new InitializationTaskWrapper(task, true));
    }

    CompletableFuture<FrameInitializationContext> run() {
        performNextTask();
        return completableFuture;
    }

    public interface InitializationTask {
        void perform(FrameInitializationContext context) throws Throwable;
    }

    private static class InitializationTaskWrapper {
        InitializationTask task;
        boolean isBackgroundTask;

        InitializationTaskWrapper(InitializationTask task, boolean isBackgroundTask) {
            this.task = task;
            this.isBackgroundTask = isBackgroundTask;
        }
    }
}
