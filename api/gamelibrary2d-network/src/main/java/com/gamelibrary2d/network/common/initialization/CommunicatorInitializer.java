package com.gamelibrary2d.network.common.initialization;

import com.gamelibrary2d.common.functional.Factory;
import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.network.common.Message;

import java.io.IOException;

/**
 * Used to configure the communicator initialization pipeline by adding {@link ConsumerTask consumer} and {@link ProducerTask producer} tasks.
 */
public interface CommunicatorInitializer {

    /**
     * Adds a {@link ConsumerTask} to the pipeline.
     */
    void addConsumer(ConsumerTask task);

    /**
     * Adds a {@link ConsumerTask} to the pipeline. The task will run if the given condition is met.
     */
    void addConsumer(TaskCondition condition, ConsumerTask task);

    /**
     * Adds a {@link ProducerTask} to the pipeline.
     */
    void addProducer(ProducerTask task);

    /**
     * Adds a {@link ProducerTask} to the pipeline. The task will run if the given condition is met.
     */
    void addProducer(TaskCondition condition, ProducerTask task);

    /**
     * Adds a {@link TaskConfiguration} to the pipeline.
     */
    default void addConfig(TaskConfiguration config) {
        config.configure(this);
    }

    /**
     * Adds a {@link ProducerTask} to the pipeline that will send a {@link Message}.
     */
    default void send(Message message) {
        send(TaskCondition.TRUE, () -> message);
    }

    /**
     * Adds a {@link ProducerTask} to the pipeline that will send a {@link Message}.
     */
    default void send(Factory<Message> factory) {
        send(TaskCondition.TRUE, factory);
    }

    /**
     * Adds a {@link ProducerTask} to the pipeline that will send a {@link Message}. The task will run if the given condition is met.
     */
    default void send(TaskCondition condition, Message message) {
        send(condition, () -> message);
    }

    /**
     * Adds a {@link ProducerTask} to the pipeline that will send a {@link Message}. The task will run if the given condition is met.
     */
    default void send(TaskCondition condition, Factory<Message> factory) {
        addProducer(condition, (ctx, com) -> factory.create().serializeMessage(com.getOutgoing()));
    }

    /**
     * Adds a {@link ConsumerTask} to the pipeline that will receive a message. The task will run if the given condition is met.
     */
    default void receive(Object key, MessageReader reader) {
        receive(TaskCondition.TRUE, key, reader);
    }

    /**
     * Adds a {@link ConsumerTask} to the pipeline that will receive a message. The task will run if the given condition is met.
     */
    default void receive(TaskCondition condition, Object key, MessageReader reader) {
        addConsumer(condition, (ctx, com, inbox) -> {
            ctx.register(key, reader.read(inbox));
            return true;
        });
    }

    interface MessageReader {
        Object read(DataBuffer buffer) throws IOException;
    }
}
