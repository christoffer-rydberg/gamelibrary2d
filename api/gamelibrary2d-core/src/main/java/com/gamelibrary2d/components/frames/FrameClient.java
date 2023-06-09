package com.gamelibrary2d.components.frames;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;

public interface FrameClient {
    void onInitializeClient(ConnectionInitializer initializer);
    void onClientInitialized(Communicator communicator);
    void onMessage(DataBuffer dataBuffer);
}
