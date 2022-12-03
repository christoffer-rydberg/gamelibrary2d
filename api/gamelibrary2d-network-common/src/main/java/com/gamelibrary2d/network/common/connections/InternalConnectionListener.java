package com.gamelibrary2d.network.common.connections;

import com.gamelibrary2d.network.common.SocketChannelConnectedHandler;
import com.gamelibrary2d.network.common.SocketChannelFailedConnectionHandler;

import java.io.IOException;
import java.nio.channels.SocketChannel;

class InternalConnectionListener {
    private final SocketChannelConnectedHandler connectedHandler;
    private final SocketChannelFailedConnectionHandler connectionFailedHandler;

    InternalConnectionListener(SocketChannelConnectedHandler connectedHandler,
                               SocketChannelFailedConnectionHandler connectionFailedHandler) {
        this.connectedHandler = connectedHandler;
        this.connectionFailedHandler = connectionFailedHandler;
    }

    void onConnected(SocketChannel socketChannel) throws IOException {
        connectedHandler.onConnected(socketChannel);
    }

    void onConnectionFailed(String endpoint, IOException exception) {
        connectionFailedHandler.onConnectionFailed(endpoint, exception);
    }
}