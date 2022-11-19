package com.gamelibrary2d.network.client;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializationContext;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;
import com.gamelibrary2d.network.common.server.AbstractServer;
import com.gamelibrary2d.network.common.server.ServerLogic;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public final class LocalServer extends AbstractServer implements Connectable {
    public static final String LOCAL_CONNECTION_ENDPOINT = "local";
    private final ServerLogic serverLogic;
    private final ArrayList<Communicator> clientSideCommunicators = new ArrayList<>();
    private boolean connectionsEnabled;

    public LocalServer(ServerLogic serverLogic) {
        this.serverLogic = serverLogic;
    }

    @Override
    public Future<Communicator> connect() {
        if (!connectionsEnabled || !serverLogic.acceptConnection(LOCAL_CONNECTION_ENDPOINT)) {
            CompletableFuture<Communicator> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Connection refused by server"));
            return future;
        }

        InternalLocalCommunicator communicator = new InternalLocalCommunicator();
        clientSideCommunicators.add(communicator);
        addPendingCommunicator(communicator.getServerSideCommunicator());
        return CompletableFuture.completedFuture(communicator);
    }

    @Override
    protected void onStart() {
        serverLogic.onStarted(this);
    }

    @Override
    protected void onStop() {
        for (Communicator com : clientSideCommunicators) {
            com.disconnect();
        }
        serverLogic.onStopped();
    }

    @Override
    protected void onClientAuthenticated(CommunicatorInitializationContext context, Communicator communicator) {
        serverLogic.onClientAuthenticated(context, communicator);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (serverLogic != null) {
            serverLogic.onUpdate(deltaTime);
        }
    }

    @Override
    protected void onConnected(Communicator communicator) {
        serverLogic.onConnected(communicator);
    }

    @Override
    protected void onInitializeClient(CommunicatorInitializer initializer) {
        serverLogic.onInitializeClient(initializer);
    }

    @Override
    protected void onClientInitialized(CommunicatorInitializationContext context, Communicator communicator) {
        serverLogic.onClientInitialized(context, communicator);
    }

    @Override
    protected void onDisconnected(Communicator communicator, boolean pending) {
        serverLogic.onDisconnected(communicator, pending);
    }

    @Override
    protected void onMessage(Communicator communicator, DataBuffer buffer) {
        serverLogic.onMessage(communicator, buffer);
    }

    @Override
    public void enableConnections() {
        connectionsEnabled = true;
    }

    @Override
    public void disableConnections() {
        connectionsEnabled = false;
    }
}
