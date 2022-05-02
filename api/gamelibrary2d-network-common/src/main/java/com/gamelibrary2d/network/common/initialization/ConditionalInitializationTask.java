package com.gamelibrary2d.network.common.initialization;

public class ConditionalInitializationTask {
    public final InitializationTask task;
    public final TaskCondition condition;

    public ConditionalInitializationTask(InitializationTask task, TaskCondition condition) {
        this.task = task;
        this.condition = condition;
    }
}
