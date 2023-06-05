package com.gamelibrary2d.components.frames;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.initialization.CommunicatorInitializer;

public interface FrameClient {
    void onInitializeClient(CommunicatorInitializer initializer);
    void onCommunicatorReady(Communicator communicator);
    void onMessage(DataBuffer dataBuffer);
}
