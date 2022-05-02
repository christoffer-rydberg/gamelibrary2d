package com.gamelibrary2d.network.common.initialization;

import com.gamelibrary2d.network.common.Communicator;

import java.io.IOException;

public interface InitializationTaskRunner {

    /**
     * Runs a {@link ConditionalInitializationTask}.
     *
     * @param communicator    The communicator.
     * @param conditionalTask The task to run.
     * @return True if the task runs to completion. False if the communicator must await more data.
     */
    boolean run(CommunicatorInitializationContext context, Communicator communicator, ConditionalInitializationTask conditionalTask) throws IOException;

}