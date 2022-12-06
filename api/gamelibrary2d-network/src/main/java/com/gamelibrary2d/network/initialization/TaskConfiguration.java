package com.gamelibrary2d.network.initialization;

/**
 * Defines a configuration of related {@link ConsumerTask ConsumerTasks} and/or {@link ProducerTask ProducerTasks}.
 */
public interface TaskConfiguration {

    void addTasks(CommunicatorInitializer initializer);

}
