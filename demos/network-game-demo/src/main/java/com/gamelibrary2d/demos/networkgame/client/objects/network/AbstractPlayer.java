package com.gamelibrary2d.demos.networkgame.client.objects.network;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.demos.networkgame.client.frames.DemoFrameClient;

public abstract class AbstractPlayer extends AbstractClientObject {

    protected AbstractPlayer(byte objectIdentifier, DemoFrameClient client, DataBuffer buffer) {
        super(objectIdentifier, client, true, buffer);
        getParticleHotspot().set(0, -30);
    }
}