package com.gamelibrary2d.network.common.server;

import com.gamelibrary2d.common.functional.ParameterizedAction;
import com.gamelibrary2d.network.common.AbstractNetworkCommunicator;
import com.gamelibrary2d.network.common.NetworkService;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;

import java.nio.channels.SocketChannel;

public class ServerSideCommunicator extends AbstractNetworkCommunicator {

    private final String endpoint;
    private final ParameterizedAction<CommunicatorInitializer> configureAuthentication;

    public ServerSideCommunicator(NetworkService networkService, SocketChannel socketChannel, ParameterizedAction<CommunicatorInitializer> configureAuthentication) {
        super(networkService, 1, false);
        setSocketChannel(socketChannel);
        endpoint = socketChannel.socket().getInetAddress().getHostAddress();
        this.configureAuthentication = configureAuthentication;
        getNetworkService().connect(getSocketChannel(), this, this::onSocketChannelDisconnected);
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