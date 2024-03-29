package com.gamelibrary2d.network.connections;

import com.gamelibrary2d.io.DataBuffer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

interface InternalConnection {
    int readIncoming() throws IOException;

    void addOutgoing(Selector selector, DataBuffer data) throws IOException;

    void sendOutgoing(SelectionKey key) throws IOException;

    void disconnect();

    void disconnect(IOException e);
}

