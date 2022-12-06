package com.gamelibrary2d.network.connections;

import java.nio.channels.ServerSocketChannel;

public class ConnectionListenerRegistration {
    private final ServerSocketChannel serverSocketChannel;
    private final int localPort;

    ConnectionListenerRegistration(ServerSocketChannel serverSocketChannel, int localPort) {
        this.serverSocketChannel = serverSocketChannel;
        this.localPort = localPort;
    }

    ServerSocketChannel getServerSocketChannel() {
        return serverSocketChannel;
    }

    public int getLocalPort() {
        return localPort;
    }

}
