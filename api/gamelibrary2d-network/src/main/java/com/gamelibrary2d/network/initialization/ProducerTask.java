package com.gamelibrary2d.network.initialization;

import com.gamelibrary2d.network.Communicator;

import java.io.IOException;

/**
 * Defines a task that doesn't read incoming data.
 * It can for example be used to send data.
 * In order to read data, use a {@link ConsumerTask}.
 */
public interface ProducerTask extends InitializationTask {

    /**
     * Runs the producer task.
     *
     * @param context      Accumulated through each task.
     * @param communicator The communicator.
     */
    void run(ConnectionContext context, Communicator communicator) throws IOException;

}