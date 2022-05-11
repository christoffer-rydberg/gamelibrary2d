package com.gamelibrary2d.network.common.server;

import com.gamelibrary2d.network.common.initialization.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class InternalCommunicatorInitializer implements CommunicatorInitializer {

    private final List<ConditionalInitializationTask> tasks = new ArrayList<>();

    @Override
    public void addConsumer(ConsumerTask task) {
        addConsumer(TaskCondition.TRUE, task);
    }

    @Override
    public void addConsumer(TaskCondition condition, ConsumerTask task) {
        tasks.add(new ConditionalInitializationTask(task, condition));
    }

    @Override
    public void addProducer(ProducerTask task) {
        addProducer(TaskCondition.TRUE, task);
    }

    @Override
    public void addProducer(TaskCondition condition, ProducerTask task) {
        tasks.add(new ConditionalInitializationTask(task, condition));
    }

    public Collection<ConditionalInitializationTask> getAll() {
        return tasks;
    }
}