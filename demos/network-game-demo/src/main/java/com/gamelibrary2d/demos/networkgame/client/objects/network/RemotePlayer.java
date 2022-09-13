package com.gamelibrary2d.demos.networkgame.client.objects.network;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.demos.networkgame.client.frames.game.GameFrameClient;
import com.gamelibrary2d.demos.networkgame.client.objects.network.decoration.RendererMap;

public class RemotePlayer extends AbstractPlayer {

    public RemotePlayer(byte primaryType, GameFrameClient client, DataBuffer buffer) {
        super(primaryType, client, buffer);
    }

    @Override
    public void setRenderer(RendererMap rendererMap) {
        rendererMap.setRenderer(this);
    }

}
