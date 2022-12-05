package com.gamelibrary2d.network.common.server;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializationContext;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;

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