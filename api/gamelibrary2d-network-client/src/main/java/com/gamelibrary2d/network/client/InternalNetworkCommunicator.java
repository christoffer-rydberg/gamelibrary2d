package com.gamelibrary2d.network.client;

import com.gamelibrary2d.functional.ParameterizedAction;
import com.gamelibrary2d.network.AbstractNetworkCommunicator;
import com.gamelibrary2d.network.connections.ConnectionService;
import com.gamelibrary2d.network.initialization.CommunicatorInitializer;

import java.io.IOException;
import java.nio.channels.SocketChannel;

class InternalNetworkCommunicator extends AbstractNetworkCommunicator {
    private final String endpoint;
    private final ParameterizedAction<CommunicatorInitializer> configureAuthentication;

    InternalNetworkCommunicator(
            String endpoint,
            ConnectionService connectionService,
            boolean ownsConnectionService,
            ParameterizedAction<CommunicatorInitializer> configureAuthentication) {
        super(connectionService, 2, ownsConnectionService);
        this.endpoint = endpoint;
        this.configureAuthentication = configureAuthentication;
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
    public void configureAuthentication(CommunicatorInitializer initializer) {
        if (configureAuthentication != null) {
            configureAuthentication.perform(initializer);
        }
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }
}