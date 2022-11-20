package com.gamelibrary2d.network.common.server;

import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.initialization.*;

import java.io.IOException;
import java.util.*;

class InternalCommunicatorInitializer implements CommunicatorInitializer {
    private final Deque<ConditionalInitializationTask> tasks = new ArrayDeque<>();
    private int initializationRetries = 100;
    private int initializationRetryDelay = 100;

    @Override
    public int getRetries() {
        return initializationRetries;
    }

    @Override
    public void setRetries(int retries) {
        this.initializationRetries = retries;
    }

    @Override
    public int getRetryDelay() {
        return initializationRetryDelay;
    }

    @Override
    public void setRetryDelay(int retryDelay) {
        this.initializationRetryDelay = retryDelay;
    }

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

    InternalInitializationTaskResult runTask(
            CommunicatorInitializationContext context,
            Communicator communicator,
            InitializationTaskRunner runner)
            throws IOException {

        if (tasks.isEmpty()) {
            return InternalInitializationTaskResult.FINISHED;
        }

        ConditionalInitializationTask next = tasks.peekFirst();
        if (runner.run(context, communicator, next)) {
            tasks.pollFirst();
            return tasks.isEmpty() ? InternalInitializationTaskResult.FINISHED : InternalInitializationTaskResult.PENDING;
        }

        return InternalInitializationTaskResult.AWAITING_DATA;
    }
}