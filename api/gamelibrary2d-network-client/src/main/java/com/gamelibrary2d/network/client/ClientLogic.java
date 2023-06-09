package com.gamelibrary2d.network.client;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;

public interface ClientLogic {
    void onInitialize(ConnectionInitializer initializer);

    void onMessage(DataBuffer buffer);
}
