package com.gamelibrary2d.demos.networkgame.client.objects.network;

import com.gamelibrary2d.demos.networkgame.client.frames.game.GameFrameClient;
import com.gamelibrary2d.io.DataBuffer;

public class Portal extends AbstractClientObject {

    public Portal(byte primaryType, GameFrameClient client, DataBuffer buffer) {
        super(primaryType, client, buffer);
    }

}
