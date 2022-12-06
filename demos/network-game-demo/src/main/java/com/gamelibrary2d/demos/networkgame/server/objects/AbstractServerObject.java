package com.gamelibrary2d.demos.networkgame.server.objects;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.io.DataBuffer;

public abstract class AbstractServerObject implements ServerObject {
    private final byte primaryType;
    private final Point velocity = new Point();
    private final Point position = new Point();

    private int id = Integer.MAX_VALUE;

    private Rectangle bounds;

    private byte secondaryType;
    private float rotation;
    private float direction;
    private boolean destroyed = true;

    protected AbstractServerObject(byte primaryType) {
        this(primaryType, (byte) 0);
    }

    protected AbstractServerObject(byte primaryType, byte secondaryType) {
        this.primaryType = primaryType;
        this.secondaryType = secondaryType;
    }

    protected void setSecondaryType(byte secondaryType) {
        this.secondaryType = secondaryType;
    }

    @Override
    public byte getObjectIdentifier() {
        return primaryType;
    }

    @Override
    public void update(float deltaTime) {
        getPosition().add(velocity.getX() * deltaTime, velocity.getY() * deltaTime);
    }

    @Override
    public boolean canCollide() {
        return !isDestroyed();
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    @Override
    public float getDirection() {
        return direction;
    }

    @Override
    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = normalizeAngle(rotation);
    }

    protected void setSpeedAndDirection(float speed, float direction) {
        this.direction = normalizeAngle(direction);
        velocity.set(0, speed);
        velocity.rotate(direction);
    }

    protected Point getVelocity() {
        return velocity;
    }

    protected void setSpeed(float x, float y) {
        velocity.set(x, y);
        onVelocityChanged();
    }

    protected void onVelocityChanged() {
        direction = normalizeAngle(velocity.getAngleDegrees());
    }

    protected void accelerate(float x, float y) {
        setSpeed(velocity.getX() + x, velocity.getY() + y);
    }

    private float normalizeAngle(float direction) {
        return (((direction % 360f) + 360f) % 360f);
    }

    @Override
    public void onRegistered(final int id) {
        this.id = id;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    @Override
    public Point getPosition() {
        return position;
    }

    public int getId() {
        return id;
    }

    @Override
    public void serialize(DataBuffer buffer) {
        buffer.put(secondaryType);
        buffer.putInt(id);
        buffer.putFloat(position.getX());
        buffer.putFloat(position.getY());
    }

    @Override
    public boolean isAccelerating() {
        return false;
    }
}
