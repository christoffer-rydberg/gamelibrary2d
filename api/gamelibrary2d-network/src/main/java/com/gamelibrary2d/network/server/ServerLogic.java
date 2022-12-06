package com.gamelibrary2d.network.server;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.initialization.CommunicatorInitializationContext;
import com.gamelibrary2d.network.initialization.CommunicatorInitializer;

import java.io.IOException;

public interface ServerLogic {

    void onStart(Host host) throws IOException;

    void onStop();

    boolean acceptConnection(String endpoint);

    void onConnectionFailed(String endpoint, Exception e);

    void onConnected(Communicator communicator);

    void onAuthenticateClient(CommunicatorInitializer initializer);

    void onInitializeClient(CommunicatorInitializer initializer);

    void onClientAuthenticated(CommunicatorInitializationContext context, Communicator communicator);

    void onClientInitialized(CommunicatorInitializationContext context, Communicator communicator);

    void onUpdate(float deltaTime);

    void onMessage(Communicator communicator, DataBuffer buffer);

    void onDisconnected(Communicator communicator, boolean pending, Throwable cause);
}
