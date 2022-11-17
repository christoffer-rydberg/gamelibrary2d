package com.gamelibrary2d.network.common.initialization;

import com.gamelibrary2d.common.functional.Factory;
import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.io.Serializable;

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
        config.addTasks(this);
    }

    /**
     * Adds a {@link ProducerTask} to the pipeline that will send a {@link Serializable} message.
     */
    default void send(Serializable message) {
        send(TaskCondition.TRUE, () -> message);
    }

    /**
     * Adds a {@link ProducerTask} to the pipeline that will send a {@link Serializable} message.
     */
    default void send(Factory<Serializable> factory) {
        send(TaskCondition.TRUE, factory);
    }

    /**
     * Adds a {@link ProducerTask} to the pipeline that will send a {@link Serializable} message. The task will run if the given condition is met.
     */
    default void send(TaskCondition condition, Serializable message) {
        send(condition, () -> message);
    }

    /**
     * Adds a {@link ProducerTask} to the pipeline that will send a {@link Serializable} message. The task will run if the given condition is met.
     */
    default void send(TaskCondition condition, Factory<Serializable> factory) {
        addProducer(condition, (ctx, com) -> factory.create().serialize(com.getOutgoing()));
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
