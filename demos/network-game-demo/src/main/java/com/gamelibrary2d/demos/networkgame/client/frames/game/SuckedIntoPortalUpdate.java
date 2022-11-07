package com.gamelibrary2d.demos.networkgame.client.frames.game;

import com.gamelibrary2d.demos.networkgame.client.objects.network.ClientObject;
import com.gamelibrary2d.updates.AbstractUpdate;

class SuckedIntoPortalUpdate extends AbstractUpdate {
    private final ClientObject target;
    private final float originX;
    private final float originY;
    private final float goalX;
    private final float goalY;

    public SuckedIntoPortalUpdate(float duration, ClientObject target, float goalX, float goalY) {
        super(duration);
        this.target = target;
        this.originX = target.getPosition().getX();
        this.originY = target.getPosition().getY();
        this.goalX = goalX;
        this.goalY = goalY;
    }

    @Override
    protected void initialize() {

    }

    @Override
    protected void onUpdate(float deltaTime) {
        float alpha = getTime() / getDuration();
        target.getPosition().lerp(originX, originY, goalX, goalY, alpha);
        target.setScale(1f - alpha);
    }
}
