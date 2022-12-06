package com.gamelibrary2d.network.client;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.initialization.CommunicatorInitializer;

public interface ClientLogic {
    void onInitialize(CommunicatorInitializer initializer);

    void onMessage(DataBuffer buffer);
}
