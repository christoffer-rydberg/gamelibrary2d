package com.gamelibrary2d.components.frames;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DefaultFrameInitializer implements FrameInitializer {
    private final Frame frame;
    private final CompletableFuture<FrameInitializationContext> completableFuture = new CompletableFuture<>();
    private final ConcurrentLinkedQueue<InitializationTaskWrapper> tasks = new ConcurrentLinkedQueue<>();
    private final FrameInitializationContext context = new DefaultFrameInitializationContext();

    public DefaultFrameInitializer(Frame frame) {
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
                performTask(task.step);
                performNextTask();
            });
        } else {
            performTask(task.step);
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

    @Override
    public void addTask(InitializationTask task) {
        tasks.add(new InitializationTaskWrapper(task, false));
    }

    @Override
    public void addTaskAsync(InitializationTask task) {
        tasks.add(new InitializationTaskWrapper(task, true));
    }

    CompletableFuture<FrameInitializationContext> run() {
        performNextTask();
        return completableFuture;
    }

    private static class InitializationTaskWrapper {
        InitializationTask step;
        boolean isAsync;

        InitializationTaskWrapper(InitializationTask step, boolean isAsync) {
            this.step = step;
            this.isAsync = isAsync;
        }
    }

    private static class DefaultFrameInitializationContext implements FrameInitializationContext {
        private final ConcurrentHashMap<Object, Object> register;

        public DefaultFrameInitializationContext() {
            register = new ConcurrentHashMap<>();
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
    }
}
