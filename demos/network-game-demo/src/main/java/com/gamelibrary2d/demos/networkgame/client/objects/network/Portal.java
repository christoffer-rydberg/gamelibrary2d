package com.gamelibrary2d.demos.networkgame.client.objects.network;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.demos.networkgame.client.frames.DemoFrameClient;

public class Portal extends AbstractClientObject {

    public Portal(byte objectIdentifier, DemoFrameClient client, DataBuffer buffer) {
        super(objectIdentifier, client, false, buffer);
    }

}
