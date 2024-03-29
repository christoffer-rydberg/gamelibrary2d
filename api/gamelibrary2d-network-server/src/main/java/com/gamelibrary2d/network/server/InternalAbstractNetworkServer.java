package com.gamelibrary2d.network.server;

import com.gamelibrary2d.network.connections.ConnectionListenerRegistration;
import com.gamelibrary2d.network.connections.ConnectionService;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;

import java.io.IOException;
import java.nio.channels.SocketChannel;

abstract class InternalAbstractNetworkServer extends AbstractServer {
    private final ConnectionService connectionService = new ConnectionService();
    private final String hostname;
    private ConnectionListenerRegistration registration;

    protected InternalAbstractNetworkServer(String hostname) {
        this.hostname = hostname;
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

    public void enableConnections(int port) throws IOException {
        throwIfNotRunning();

        disableConnections();

        registration = connectionService.registerConnectionListener(
                hostname,
                port,
                this::onConnected,
                (endpoint, exc) -> invokeLater(() -> onConnectionFailed(endpoint, exc)));

        onConnectionsEnabled(registration.getLocalPort());
    }

    public void disableConnections() throws IOException {
        if (registration != null) {
            deregisterConnectionListener();
            onConnectionsDisabled();
        }
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
        connectionService.stop();
    }

    private void deregisterConnectionListener() throws IOException {
        if (registration != null) {
            connectionService.deregisterConnectionListener(registration);
            registration = null;
        }
    }

    protected abstract void onConnectionsEnabled(int port);

    protected abstract void onConnectionsDisabled();

    protected abstract void authenticateClient(ConnectionInitializer initializer);

    protected abstract boolean acceptConnection(String endpoint);

    protected abstract void onConnectionFailed(String endpoint, Exception e);
}