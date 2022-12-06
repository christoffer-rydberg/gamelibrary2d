package com.gamelibrary2d.network.server;

import com.gamelibrary2d.functional.ParameterizedAction;
import com.gamelibrary2d.network.AbstractNetworkCommunicator;
import com.gamelibrary2d.network.connections.ConnectionService;
import com.gamelibrary2d.network.initialization.CommunicatorInitializer;

import java.nio.channels.SocketChannel;

class InternalNetworkCommunicator extends AbstractNetworkCommunicator {

    private final String endpoint;
    private final ParameterizedAction<CommunicatorInitializer> configureAuthentication;

    public InternalNetworkCommunicator(ConnectionService connectionService, SocketChannel socketChannel, ParameterizedAction<CommunicatorInitializer> configureAuthentication) {
        super(connectionService, 1, false);
        setSocketChannel(socketChannel);
        endpoint = socketChannel.socket().getInetAddress().getHostAddress();
        this.configureAuthentication = configureAuthentication;
        getConnectionService().connect(getSocketChannel(), this, this::onSocketChannelDisconnected);
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public void configureAuthentication(CommunicatorInitializer initializer) {
        configureAuthentication.perform(initializer);
    }

}