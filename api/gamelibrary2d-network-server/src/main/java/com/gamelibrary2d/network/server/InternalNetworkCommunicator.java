package com.gamelibrary2d.network.server;

import com.gamelibrary2d.network.AbstractNetworkCommunicator;
import com.gamelibrary2d.network.Authenticator;
import com.gamelibrary2d.network.connections.ConnectionService;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;

import java.nio.channels.SocketChannel;

class InternalNetworkCommunicator extends AbstractNetworkCommunicator {

    private final String endpoint;
    private final Authenticator authenticator;

    public InternalNetworkCommunicator(ConnectionService connectionService, SocketChannel socketChannel, Authenticator authenticator) {
        super(connectionService, 1);
        setSocketChannel(socketChannel);
        endpoint = socketChannel.socket().getInetAddress().getHostAddress();
        this.authenticator = authenticator;
        getConnectionService().connect(getSocketChannel(), this, this::onSocketChannelDisconnected);
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public void addAuthentication(ConnectionInitializer initializer) {
        authenticator.addAuthentication(initializer);
    }

}