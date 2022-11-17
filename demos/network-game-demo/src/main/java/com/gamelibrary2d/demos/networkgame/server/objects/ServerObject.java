package com.gamelibrary2d.demos.networkgame.server.objects;

import com.gamelibrary2d.collision.Collidable;
import com.gamelibrary2d.collision.CollisionDetection;
import com.gamelibrary2d.common.io.Serializable;
import com.gamelibrary2d.common.denotations.Positionable;
import com.gamelibrary2d.common.denotations.Rotatable;

public interface ServerObject extends Collidable, Rotatable, Positionable, Serializable {

    int getId();

    void onRegistered(int id);

    byte getObjectIdentifier();

    float getDirection();

    float getRotation();

    boolean isDestroyed();

    void setDestroyed(boolean destroyed);

    void addCollisionDetection(CollisionDetection collisionDetection);

    boolean isAccelerating();

    @Override
    default float getPosX() {
        return getPosition().getX();
    }

    @Override
    default float getPosY() {
        return getPosition().getY();
    }

    @Override
    default void setPosition(float x, float y) {
        getPosition().set(x, y);
    }
}
