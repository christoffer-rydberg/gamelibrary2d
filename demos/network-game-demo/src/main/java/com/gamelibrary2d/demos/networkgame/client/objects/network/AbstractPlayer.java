package com.gamelibrary2d.demos.networkgame.client.objects.network;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.demos.networkgame.client.frames.game.GameFrameClient;
import com.gamelibrary2d.interpolation.RotationInterpolator;
import com.gamelibrary2d.io.DataBuffer;

public abstract class AbstractPlayer extends AbstractClientObject {
    private final GameFrameClient client;
    private final RotationInterpolator rotationInterpolator = new RotationInterpolator(this);
    private final Color color;

    protected AbstractPlayer(byte primaryType, GameFrameClient client, DataBuffer buffer) {
        super(primaryType, client, buffer);
        this.client = client;
        getParticleHotspot().set(0, -30);
        color = new Color(buffer.getFloat(), buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
    }

    public Color getColor() {
        return color;
    }

    @Override
    public void setGoalRotation(float rotation) {
        rotationInterpolator.setGoal(rotation, 1f / client.getServerUpdatesPerSecond());
    }

    @Override
    public void update(float deltaTime) {
        rotationInterpolator.update(deltaTime);
        super.update(deltaTime);
    }

    @Override
    protected boolean useUpdateEffect() {
        return super.useUpdateEffect() && isAccelerating();
    }
}
