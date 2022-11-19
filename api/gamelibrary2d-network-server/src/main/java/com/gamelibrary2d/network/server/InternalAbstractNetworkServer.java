package com.gamelibrary2d.network.server;

import com.gamelibrary2d.network.common.NetworkService;
import com.gamelibrary2d.network.common.ServerSocketChannelRegistration;
import com.gamelibrary2d.network.common.server.AbstractServer;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;

import java.io.IOException;
import java.nio.channels.SocketChannel;

abstract class InternalAbstractNetworkServer extends AbstractServer {
    private final NetworkService networkService;
    private final boolean ownsNetworkService;
    private final int port;
    private final String hostname;

    private ServerSocketChannelRegistration registration;

    private InternalAbstractNetworkServer(String hostname, int port, NetworkService networkService, boolean ownsNetworkService) {
        this.hostname = hostname;
        this.port = port;
        this.networkService = networkService;
        this.ownsNetworkService = ownsNetworkService;
    }

    protected InternalAbstractNetworkServer(String hostname, int port) {
        this(hostname, port, new NetworkService(), true);
    }

    protected InternalAbstractNetworkServer(String hostname, int port, NetworkService networkService) {
        this(hostname, port, networkService, false);
    }

    private void onConnected(SocketChannel channel) {
        invokeLater(() -> {
            String endpoint = channel.socket().getInetAddress().getHostAddress();
            if (!acceptConnection(endpoint)) {
                networkService.disconnect(channel);
                onConnectionFailed(endpoint, new IOException("Connection refused by server"));
            } else {
                try {
                    // Disable Nagle's algorithm
                    channel.socket().setTcpNoDelay(true);
                    addPendingCommunicator(new InternalNetworkCommunicator(
                            networkService,
                            channel,
                            this::authenticateClient));
                } catch (IOException e) {
                    networkService.disconnect(channel);
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
        registration = networkService.registerConnectionListener(
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
        networkService.start();
    }

    @Override
    protected void onStop() throws IOException, InterruptedException {
        deregisterConnectionListener();
        if (ownsNetworkService) {
            networkService.stop();
        }
    }

    private void deregisterConnectionListener() throws IOException {
        if (registration != null) {
            networkService.deregisterConnectionListener(registration);
            registration = null;
        }
    }

    protected abstract void authenticateClient(CommunicatorInitializer initializer);

    protected abstract boolean acceptConnection(String endpoint);

    protected abstract void onConnectionFailed(String endpoint, Exception e);
}