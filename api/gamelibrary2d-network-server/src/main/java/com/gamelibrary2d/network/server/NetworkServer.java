package com.gamelibrary2d.network.server;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.connections.ConnectionService;
import com.gamelibrary2d.network.common.server.Host;
import com.gamelibrary2d.network.common.server.ServerLogic;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializationContext;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;

import java.io.IOException;
import java.util.List;

public final class NetworkServer extends InternalAbstractNetworkServer {
    private final ServerLogic serverLogic;
    private final Host host = new InternalHost();

    public NetworkServer(String hostname, int port, ServerLogic serverLogic) {
        super(hostname, port);
        this.serverLogic = serverLogic;
    }

    public NetworkServer(String hostname, int port, ConnectionService connectionService, ServerLogic serverLogic) {
        super(hostname, port, connectionService);
        this.serverLogic = serverLogic;
    }

    @Override
    protected void authenticateClient(CommunicatorInitializer initializer) {
        serverLogic.onAuthenticateClient(initializer);
    }

    @Override
    protected boolean acceptConnection(String endpoint) {
        return serverLogic.acceptConnection(endpoint);
    }

    @Override
    protected void onConnectionFailed(String endpoint, Exception e) {
        serverLogic.onConnectionFailed(endpoint, e);
    }

    @Override
    protected void onClientAuthenticated(CommunicatorInitializationContext context, Communicator communicator) {
        serverLogic.onClientAuthenticated(context, communicator);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        serverLogic.onUpdate(deltaTime);
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
    protected void onDisconnected(Communicator communicator, boolean pending, Throwable cause) {
        serverLogic.onDisconnected(communicator, pending, cause);
    }

    @Override
    protected void onStart() throws IOException {
        super.onStart();
        serverLogic.onStart(host);
    }

    @Override
    protected void onStop() throws IOException, InterruptedException {
        serverLogic.onStop();
        super.onStop();
    }

    @Override
    protected void onMessage(Communicator communicator, DataBuffer buffer) {
        serverLogic.onMessage(communicator, buffer);
    }

    private class InternalHost implements Host {

        @Override
        public void enableConnections() throws IOException {
            NetworkServer.super.enableConnections();
        }

        @Override
        public void disableConnections() throws IOException {
            NetworkServer.super.disableConnections();
        }

        @Override
        public void reinitialize(Communicator communicator) {
            NetworkServer.super.reinitialize(communicator);
        }

        @Override
        public List<Communicator> getCommunicators() {
            return NetworkServer.super.getCommunicators();
        }

        @Override
        public DataBuffer getStreamBuffer() {
            return NetworkServer.super.getStreamBuffer();
        }
    }
}