package com.gamelibrary2d.network.initialization;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.Communicator;

import java.io.IOException;

/**
 * Defines a task that reads incoming data and optionally sends data.
 * Note that the task will not run until data is available in the inbox.
 * To send data, without reading, use a {@link ProducerTask}.
 */
public interface ConsumerTask extends InitializationTask {

    /**
     * Runs the consumer task. This method is invoked when data is
     * available in the inbox buffer. It will be invoked repeatedly until the task
     * is completed, which is indicated by the return value.
     *
     * @param context      Accumulated through each task.
     * @param communicator The communicator.
     * @param inbox        The inbox buffer.
     * @return True if the task has completed, false otherwise.
     */
    boolean run(ConnectionContext context, Communicator communicator, DataBuffer inbox) throws IOException;
}
