package com.gamelibrary2d.network.client;

import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;
import com.gamelibrary2d.network.common.server.Server;

public interface LocalServer extends Server {

    void connectCommunicator(Communicator communicator);

    void configureClientAuthentication(CommunicatorInitializer initializer);

}
