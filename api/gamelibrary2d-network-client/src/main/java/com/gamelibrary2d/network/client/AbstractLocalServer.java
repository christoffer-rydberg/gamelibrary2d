package com.gamelibrary2d.network.client;

import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.server.AbstractServer;

public abstract class AbstractLocalServer extends AbstractServer implements LocalServer {

    private Communicator communicator;

    @Override
    public void connectCommunicator(Communicator communicator) {
        super.addPendingCommunicator(communicator);
        this.communicator = communicator;
    }

    @Override
    protected void onStop() {
        if (communicator != null) {
            communicator.disconnect();
        }
    }
}
