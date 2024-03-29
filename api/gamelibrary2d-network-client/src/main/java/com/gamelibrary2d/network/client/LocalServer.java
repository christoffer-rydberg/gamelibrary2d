package com.gamelibrary2d.network.client;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.Authenticator;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.initialization.ConnectionContext;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;
import com.gamelibrary2d.network.server.AbstractServer;
import com.gamelibrary2d.network.server.Host;
import com.gamelibrary2d.network.server.ServerLogic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public final class LocalServer extends AbstractServer implements ConnectionFactory {
    public static final String LOCAL_CONNECTION_ENDPOINT = "local";
    private final ServerLogic serverLogic;
    private final Host host = new InternalHost();
    private final ArrayList<Communicator> clientSideCommunicators = new ArrayList<>();
    private final InternalLocalAuthenticator authentication;
    private boolean connectionsEnabled;

    public LocalServer(ServerLogic serverLogic) {
        this.serverLogic = serverLogic;
        authentication = null;
    }

    public LocalServer(ServerLogic serverLogic, Authenticator clientSideAuthenticator, Authenticator serverSideAuthenticator) {
        this.serverLogic = serverLogic;
        authentication = new InternalLocalAuthenticator(clientSideAuthenticator, serverSideAuthenticator);
    }

    @Override
    public Future<Communicator> createConnection() {
        if (!connectionsEnabled || !serverLogic.acceptConnection(LOCAL_CONNECTION_ENDPOINT)) {
            CompletableFuture<Communicator> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Connection refused by server"));
            return future;
        }

        InternalLocalCommunicator communicator = new InternalLocalCommunicator(authentication);
        clientSideCommunicators.add(communicator);
        addPendingCommunicator(communicator.getServerSideCommunicator());
        return CompletableFuture.completedFuture(communicator);
    }

    @Override
    protected void onStart() throws IOException {
        serverLogic.onStart(host);
    }

    @Override
    protected void onStop() {
        for (Communicator com : clientSideCommunicators) {
            com.disconnect();
        }
        serverLogic.onStop();
    }

    @Override
    protected void onClientAuthenticated(ConnectionContext context, Communicator communicator) {
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
    protected void onInitializeClient(ConnectionInitializer initializer) {
        serverLogic.onInitializeClient(initializer);
    }

    @Override
    protected void onClientInitialized(ConnectionContext context, Communicator communicator) {
        serverLogic.onClientInitialized(context, communicator);
    }

    @Override
    protected void onDisconnected(Communicator communicator, boolean pending, Throwable cause) {
        serverLogic.onDisconnected(communicator, pending, cause);
    }

    @Override
    protected void onMessage(Communicator communicator, DataBuffer buffer) {
        serverLogic.onMessage(communicator, buffer);
    }

    private class InternalHost implements Host {

        @Override
        public String getHostName() {
            return "localhost";
        }

        @Override
        public void enableConnections(int port) {
            if (!connectionsEnabled) {
                connectionsEnabled = true;
                serverLogic.onConnectionsEnabled(port);
            }
        }

        @Override
        public void disableConnections() {
            if (connectionsEnabled) {
                connectionsEnabled = false;
                serverLogic.onConnectionsDisabled();
            }
        }

        @Override
        public void reinitialize(Communicator communicator) {
            LocalServer.super.reinitialize(communicator);
        }

        @Override
        public List<Communicator> getCommunicators() {
            return LocalServer.super.getCommunicators();
        }

        @Override
        public DataBuffer getStreamBuffer() {
            return LocalServer.super.getStreamBuffer();
        }
    }
}
