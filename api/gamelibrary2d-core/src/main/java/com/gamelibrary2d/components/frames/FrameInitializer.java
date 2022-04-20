package com.gamelibrary2d.components.frames;

public interface FrameInitializer {
    void addTask(InitializationTask task);
    void addTaskAsync(InitializationTask task);

    interface InitializationTask {
        void perform(FrameInitializationContext context) throws Throwable;
    }
}
