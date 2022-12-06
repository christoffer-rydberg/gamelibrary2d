package com.gamelibrary2d.network;

import java.io.IOException;

public interface SocketChannelFailedConnectionHandler {
    void onConnectionFailed(String endpoint, IOException exception);
}
