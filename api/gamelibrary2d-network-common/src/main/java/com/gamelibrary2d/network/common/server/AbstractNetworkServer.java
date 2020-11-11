package com.gamelibrary2d.network.common.server;

import com.gamelibrary2d.network.common.NetworkService;
import com.gamelibrary2d.network.common.ServerSocketChannelRegistration;
import com.gamelibrary2d.network.common.initialization.CommunicationSteps;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public abstract class AbstractNetworkServer extends InternalAbstractServer {
    private final NetworkService networkService;
    private final boolean ownsNetworkService;

    private int port;
    private ServerSocketChannelRegistration registration;

    private AbstractNetworkServer(int port, NetworkService networkService, boolean ownsNetworkService) {
        this.port = port;
        this.networkService = networkService;
        this.ownsNetworkService = ownsNetworkService;
    }

    protected AbstractNetworkServer(int port) {
        this(port, new NetworkService(), true);
    }

    protected AbstractNetworkServer(int port, NetworkService networkService) {
        this(port, networkService, false);
    }

    private void onConnected(SocketChannel channel) {
        invokeLater(() -> {
            var endpoint = channel.socket().getInetAddress().getHostAddress();
            if (!acceptConnection(endpoint)) {
                networkService.disconnect(channel);
                onConnectionFailed(endpoint, new IOException("Connection refused by server"));
            } else {
                try {
                    // Disable Nagle's algorithm
                    channel.socket().setTcpNoDelay(true);
                    addConnectedCommunicator(new ServerSideCommunicator(
                            networkService,
                            channel,
                            this::configureClientAuthentication));
                } catch (IOException e) {
                    networkService.disconnect(channel);
                    onConnectionFailed(endpoint, e);
                }
            }
        });
    }

    public boolean isListeningForConnections() {
        return registration != null;
    }

    public void listenForConnections(boolean listen) throws IOException {
        if (!isRunning()) {
            throw new IllegalStateException("Server is not running");
        }

        if (listen) {
            enableConnections();
        } else {
            disableConnections();
        }
    }

    private void enableConnections() throws IOException {
        registration = networkService.registerConnectionListener(
                "localhost",
                port,
                this::onConnected,
                (endpoint, exc) -> invokeLater(() -> onConnectionFailed(endpoint, exc)));
    }

    private void disableConnections() throws IOException {
        if (registration != null) {
            networkService.deregisterConnectionListener(registration);
            registration = null;
        }
    }

    @Override
    protected void onStart() throws IOException {
        networkService.start();
    }

    @Override
    protected void onStop() throws IOException, InterruptedException {
        disableConnections();
        if (ownsNetworkService) {
            networkService.stop();
        }
    }

    protected abstract void configureClientAuthentication(CommunicationSteps steps);

    protected abstract boolean acceptConnection(String endpoint);

    protected abstract void onConnectionFailed(String endpoint, Exception e);
}