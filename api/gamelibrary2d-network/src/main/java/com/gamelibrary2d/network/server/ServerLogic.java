package com.gamelibrary2d.network.server;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.initialization.ConnectionContext;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;

import java.io.IOException;

public interface ServerLogic {

    void onStart(Host host) throws IOException;

    void onConnectionsEnabled(int port);

    void onConnectionsDisabled();

    void onStop();

    boolean acceptConnection(String endpoint);

    void onConnectionFailed(String endpoint, Exception e);

    void onConnected(Communicator communicator);

    void onInitializeClient(ConnectionInitializer initializer);

    void onClientAuthenticated(ConnectionContext context, Communicator communicator);

    void onClientInitialized(ConnectionContext context, Communicator communicator);

    void onUpdate(float deltaTime);

    void onMessage(Communicator communicator, DataBuffer buffer);

    void onDisconnected(Communicator communicator, boolean pending, Throwable cause);
}
