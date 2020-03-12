package com.gamelibrary2d.network;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.objects.AbstractMouseAwareObject;

public abstract class AbstractClientObject extends AbstractMouseAwareObject implements ClientObject {

    private final NetworkFrame frame;

    private final int id;

    private float x0;
    private float y0;
    private float x1;
    private float y1;
    private float interpolationAlpha = -1;
    private float rotation0;
    private float rotation1;
    private float rotationDelta;
    private float rotationInterpolationAlpha = -1;

    protected AbstractClientObject(NetworkFrame frame, DataBuffer buffer) {
        this.frame = frame;
        id = buffer.getInt();
        position().set(buffer.getFloat(), buffer.getFloat());
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setGoalPosition(float x, float y) {
        x0 = position().getX();
        y0 = position().getY();
        x1 = x;
        y1 = y;
        interpolationAlpha = 0;
    }

    @Override
    public void setGoalRotation(float rotation) {
        rotation0 = getRotation();
        rotation1 = rotation;
        // Always take the shortest way to reach the correct rotation
        rotationDelta = rotation1 - rotation0;
        while (rotationDelta < -180)
            rotationDelta += 360;
        while (rotationDelta > 180)
            rotationDelta -= 360;
        rotationInterpolationAlpha = 0;
    }

    public void instantPositionUpdate() {
        if (interpolationAlpha != -1) {
            position().set(x1, y1);
            interpolationAlpha = -1;
        }
    }

    @Override
    public void update(float deltaTime) {
        onUpdate(deltaTime);
        float ups = frame.getServerUpdateRate();
        if (ups <= 0) {
            instantMovement();
        } else {
            interpolateMovement(deltaTime, ups);
        }
    }

    private void instantMovement() {
        if (interpolationAlpha != -1) {
            position().set(x1, y1);
            interpolationAlpha = -1;
        }

        if (rotationInterpolationAlpha != -1) {
            setRotation(rotation1);
            rotationInterpolationAlpha = -1;
        }
    }

    private void interpolateMovement(float deltaTime, float ups) {
        // Move towards goal position
        if (interpolationAlpha != -1) {
            interpolationAlpha += deltaTime * ups;
            if (interpolationAlpha >= 1f) {
                position().set(x1, y1);
                interpolationAlpha = -1;
            } else {
                position().set(x0, y0);
                position().lerp(x1, y1, interpolationAlpha);
            }
        }

        // Move towards goal rotation
        if (rotationInterpolationAlpha != -1) {
            rotationInterpolationAlpha += deltaTime * ups;
            if (rotationInterpolationAlpha >= 1f) {
                setRotation(rotation1);
                rotationInterpolationAlpha = -1;
            } else {
                float rotation = rotation0 + rotationDelta * rotationInterpolationAlpha;
                setRotation(rotation);
            }
        }
    }

    @Override
    protected boolean isListeningToMouseClickEvents() {
        return false;
    }

    @Override
    protected boolean isListeningToMouseHoverEvents() {
        return false;
    }

    @Override
    protected boolean isListeningToMouseDragEvents() {
        return false;
    }

    protected abstract void onUpdate(float deltaTime);
}