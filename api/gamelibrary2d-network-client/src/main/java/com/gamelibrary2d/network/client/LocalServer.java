package com.gamelibrary2d.network.client;

import com.gamelibrary2d.common.functional.Func;
import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializationContext;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;
import com.gamelibrary2d.network.common.server.AbstractServer;
import com.gamelibrary2d.network.common.server.Server;
import com.gamelibrary2d.network.common.server.ServerContext;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class LocalServer extends AbstractServer implements Connectable {
    private final Func<Server, ServerContext> serverContextFactory;
    private final ArrayList<Communicator> clientSideCommunicators = new ArrayList<>();
    private ServerContext serverContext;

    public LocalServer(Func<Server, ServerContext> serverContextFactory) {
        this.serverContextFactory = serverContextFactory;
    }

    @Override
    public Future<Communicator> connect() {
        InternalLocalCommunicator communicator = new InternalLocalCommunicator();
        clientSideCommunicators.add(communicator);
        addPendingCommunicator(communicator.getServerSideCommunicator());
        return CompletableFuture.completedFuture(communicator);
    }

    @Override
    protected void onStart() {
        serverContext = serverContextFactory.invoke(this);
        serverContext.start();
    }

    @Override
    protected void onStop() {
        for (Communicator com : clientSideCommunicators) {
            com.disconnect();
        }
        serverContext.stop();
    }

    @Override
    protected void onClientAuthenticated(CommunicatorInitializationContext context, Communicator communicator) {
        serverContext.onClientAuthenticated(context, communicator);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (serverContext != null) {
            serverContext.update(deltaTime);
        }
    }

    @Override
    protected void onConnected(Communicator communicator) {
        serverContext.onConnected(communicator);
    }

    @Override
    protected void initializeClient(CommunicatorInitializer initializer) {
        serverContext.configureClientInitialization(initializer);
    }

    @Override
    protected void onClientInitialized(CommunicatorInitializationContext context, Communicator communicator) {
        serverContext.onClientInitialized(context, communicator);
    }

    @Override
    protected void onDisconnected(Communicator communicator, boolean pending) {
        serverContext.onDisconnected(communicator, pending);
    }

    @Override
    protected void onMessage(Communicator communicator, DataBuffer buffer) {
        serverContext.onMessage(communicator, buffer);
    }
}
