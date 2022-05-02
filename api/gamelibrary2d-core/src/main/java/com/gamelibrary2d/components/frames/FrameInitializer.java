package com.gamelibrary2d.components.frames;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Used to configure the frame initialization pipeline by adding {@link FrameInitializer#addTask synchronous}
 * or {@link FrameInitializer#addAsyncTask asynchronous} {@link InitializationTask tasks}.
 * <br><br>
 * <p>
 * The initialization pipeline works as follows:
 * <br>- Each task runs sequentially in the order it was added.
 * <br>- Each update cycle will run at the most one task.
 * <br>- When a task has completed it will schedule the next task by invoking {@link Frame#invokeLater}
 * <br>- Synchronous tasks will block the update cycle, while asynchronous task are non-blocking.
 * <br>- Asynchronous tasks cannot access OpenGL context.
 * <br>- Asynchronous tasks are generally safe to have side effects. The underlying {@link CompletableFuture} assures
 * that the main thread will have visibility of changed fields when the task has completed. However, fields that are accessed
 * or modified in parallel with the task execution must be properly synchronized.
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
        if (task.isAsync) {
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
     *
     * @param task The initialization task
     */
    public void addTask(InitializationTask task) {
        tasks.add(new InitializationTaskWrapper(task, false));
    }

    /**
     * Adds an initialization task to the pipeline that will run asynchronously.
     *
     * @param task The initialization task
     */
    public void addAsyncTask(InitializationTask task) {
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
        boolean isAsync;

        InitializationTaskWrapper(InitializationTask task, boolean isAsync) {
            this.task = task;
            this.isAsync = isAsync;
        }
    }
}
