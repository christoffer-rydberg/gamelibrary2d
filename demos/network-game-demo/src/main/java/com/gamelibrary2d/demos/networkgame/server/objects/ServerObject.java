package com.gamelibrary2d.demos.networkgame.server.objects;

import com.gamelibrary2d.collision.CollisionDetection;
import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.io.Serializable;

public interface ServerObject extends Serializable {

    int getId();

    void onRegistered(int id);

    Point getPosition();

    default void setPosition(Point position) {
        getPosition().set(position);
    }

    default void setPosition(float x, float y) {
        getPosition().set(x, y);
    }

    byte getObjectIdentifier();

    float getDirection();

    float getRotation();

    boolean isDestroyed();

    void setDestroyed(boolean destroyed);

    void addCollisionDetection(CollisionDetection collisionDetection);

    boolean isAccelerating();
}
