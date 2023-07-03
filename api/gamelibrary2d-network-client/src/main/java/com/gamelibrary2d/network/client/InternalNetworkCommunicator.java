package com.gamelibrary2d.network.client;

import com.gamelibrary2d.network.AbstractNetworkCommunicator;
import com.gamelibrary2d.network.Authenticator;
import com.gamelibrary2d.network.connections.ConnectionService;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;

import java.io.IOException;
import java.nio.channels.SocketChannel;

class InternalNetworkCommunicator extends AbstractNetworkCommunicator {
    private final String endpoint;
    private final Authenticator authenticator;

    InternalNetworkCommunicator(
            String endpoint,
            ConnectionService connectionService,
            Authenticator authenticator) {
        super(connectionService, 2);
        this.endpoint = endpoint;
        this.authenticator = authenticator;
    }

    @Override
    protected void onSocketChannelDisconnected(IOException error) {
        super.onSocketChannelDisconnected(error);
    }

    @Override
    public void setSocketChannel(SocketChannel socketChannel) {
        super.setSocketChannel(socketChannel);
    }

    @Override
    public void addAuthentication(ConnectionInitializer initializer) {
        if (authenticator != null) {
            authenticator.addAuthentication(initializer);
        }
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }
}