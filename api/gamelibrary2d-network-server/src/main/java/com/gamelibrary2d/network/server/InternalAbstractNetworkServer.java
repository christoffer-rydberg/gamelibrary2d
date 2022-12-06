package com.gamelibrary2d.network.server;

import com.gamelibrary2d.network.connections.ConnectionListenerRegistration;
import com.gamelibrary2d.network.connections.ConnectionService;
import com.gamelibrary2d.network.initialization.CommunicatorInitializer;

import java.io.IOException;
import java.nio.channels.SocketChannel;

abstract class InternalAbstractNetworkServer extends AbstractServer {
    private final ConnectionService connectionService;
    private final boolean ownsConnectionService;
    private final int port;
    private final String hostname;
    private ConnectionListenerRegistration registration;

    private InternalAbstractNetworkServer(String hostname, int port, ConnectionService connectionService, boolean ownsConnectionService) {
        this.hostname = hostname;
        this.port = port;
        this.connectionService = connectionService;
        this.ownsConnectionService = ownsConnectionService;
    }

    protected InternalAbstractNetworkServer(String hostname, int port) {
        this(hostname, port, new ConnectionService(), true);
    }

    protected InternalAbstractNetworkServer(String hostname, int port, ConnectionService connectionService) {
        this(hostname, port, connectionService, false);
    }

    private void onConnected(SocketChannel channel) {
        invokeLater(() -> {
            String endpoint = channel.socket().getInetAddress().getHostAddress();
            if (!acceptConnection(endpoint)) {
                connectionService.disconnect(channel);
                onConnectionFailed(endpoint, new IOException("Connection refused by server"));
            } else {
                try {
                    // Disable Nagle's algorithm
                    channel.socket().setTcpNoDelay(true);
                    addPendingCommunicator(new InternalNetworkCommunicator(
                            connectionService,
                            channel,
                            this::authenticateClient));
                } catch (IOException e) {
                    connectionService.disconnect(channel);
                    onConnectionFailed(endpoint, e);
                }
            }
        });
    }

    public boolean isConnectionsEnabled() {
        return registration != null;
    }

    public void enableConnections() throws IOException {
        throwIfNotRunning();
        registration = connectionService.registerConnectionListener(
                hostname,
                port,
                this::onConnected,
                (endpoint, exc) -> invokeLater(() -> onConnectionFailed(endpoint, exc)));
    }

    public void disableConnections() throws IOException {
        throwIfNotRunning();
        deregisterConnectionListener();
    }

    private void throwIfNotRunning() {
        if (!isRunning()) {
            throw new IllegalStateException("Server is not running");
        }
    }

    @Override
    protected void onStart() throws IOException {
        connectionService.start();
    }

    @Override
    protected void onStop() throws IOException, InterruptedException {
        deregisterConnectionListener();
        if (ownsConnectionService) {
            connectionService.stop();
        }
    }

    private void deregisterConnectionListener() throws IOException {
        if (registration != null) {
            connectionService.deregisterConnectionListener(registration);
            registration = null;
        }
    }

    protected abstract void authenticateClient(CommunicatorInitializer initializer);

    protected abstract boolean acceptConnection(String endpoint);

    protected abstract void onConnectionFailed(String endpoint, Exception e);
}