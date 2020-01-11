package com.gamelibrary2d.updates;

public class PositionUpdate extends AbstractAttributeUpdate {

    private final float originalDeltaX;
    private final float originalDeltaY;

    private float deltaX;
    private float deltaY;

    public PositionUpdate(UpdateObject target, float deltaX, float deltaY) {
        this(target, deltaX, deltaY, true);
    }

    public PositionUpdate(UpdateObject target, float deltaX, float deltaY, boolean scaleOverDuration) {
        super(target, scaleOverDuration);
        this.originalDeltaX = deltaX;
        this.originalDeltaY = deltaY;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    @Override
    public void makeAbsolute() {
        deltaX = originalDeltaX - getTarget().getPosition().getX();
        deltaY = originalDeltaY - getTarget().getPosition().getY();
    }

    public void makeRelative(UpdateObject target) {
        deltaX = originalDeltaX - getTarget().getPosition().getX() + target.getPosition().getX();
        deltaY = originalDeltaY - getTarget().getPosition().getY() + target.getPosition().getY();
    }

    @Override
    protected void onApply(float deltaTime) {
        getTarget().getPosition().add(deltaX * deltaTime, deltaY * deltaTime);
    }
}