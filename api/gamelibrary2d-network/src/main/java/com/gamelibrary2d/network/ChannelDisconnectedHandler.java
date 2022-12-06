package com.gamelibrary2d.network;

import java.io.IOException;

public interface ChannelDisconnectedHandler {
    void onDisconnected(IOException exception);
}
