package com.gamelibrary2d.collision;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.collision.handlers.CollisionHandler;
import com.gamelibrary2d.collision.handlers.UpdatedHandler;

import java.util.ArrayList;

class InternalCollidableWrapper<T1 extends Collidable> {
    final T1 collidable;
    final Class<?> collidableClass;
    final CollidableInfo<T1> info;
    private final UpdatedHandler<T1> updatedHandler;
    private final ArrayList<CollisionHandler<T1, ?>> collisionHandlers;

    InternalCollidableWrapper(T1 collidable, Class<?> collidableClass) {
        this.collidable = collidable;
        this.collidableClass = collidableClass;
        this.updatedHandler = null;
        this.collisionHandlers = null;
        this.info = new CollidableInfo<>(collidable);
    }

    InternalCollidableWrapper(T1 collidable, Class<?> collidableClass, CollisionHandler<T1, ?> collisionHandler) {
        this.collidable = collidable;
        this.collidableClass = collidableClass;
        if (collisionHandler != null) {
            this.collisionHandlers = new ArrayList<>(1);
            collisionHandlers.add(collisionHandler);
        } else {
            this.collisionHandlers = null;
        }
        this.info = new CollidableInfo<>(collidable);
        updatedHandler = null;
    }

    InternalCollidableWrapper(T1 collidable, Class<?> collidableClass, ArrayList<CollisionHandler<T1, ?>> collisionHandlers) {
        this.collidable = collidable;
        this.collidableClass = collidableClass;
        this.collisionHandlers = collisionHandlers;
        this.info = new CollidableInfo<>(collidable);
        updatedHandler = null;
    }

    InternalCollidableWrapper(T1 collidable, Class<?> collidableClass, UpdatedHandler<T1> updatedHandler) {
        this.collidable = collidable;
        this.collidableClass = collidableClass;
        this.updatedHandler = updatedHandler;
        collisionHandlers = null;
        this.info = new CollidableInfo<>(collidable);
    }

    InternalCollidableWrapper(
            T1 collidable,
            Class<?> collidableClass,
            UpdatedHandler<T1> updatedHandler,
            CollisionHandler<T1, ?> collisionHandler) {
        this.collidable = collidable;
        this.collidableClass = collidableClass;
        this.updatedHandler = updatedHandler;
        if (collisionHandler != null) {
            this.collisionHandlers = new ArrayList<>(1);
            collisionHandlers.add(collisionHandler);
        } else {
            collisionHandlers = null;
        }
        this.info = new CollidableInfo<>(collidable);
    }

    InternalCollidableWrapper(
            T1 collidable,
            Class<?> collidableClass,
            UpdatedHandler<T1> updatedHandler,
            ArrayList<CollisionHandler<T1, ?>> collisionHandlers) {
        this.collidable = collidable;
        this.collidableClass = collidableClass;
        this.updatedHandler = updatedHandler;
        this.collisionHandlers = collisionHandlers;
        this.info = new CollidableInfo<>(collidable);
    }

    void update(float deltaTime) {
        float xBeforeUpdate = collidable.getPosX();
        float yBeforeUpdate = collidable.getPosY();
        Rectangle boundsBeforeUpdate = collidable.getBounds();

        collidable.update(deltaTime);

        info.reset(
                deltaTime,
                xBeforeUpdate,
                yBeforeUpdate,
                boundsBeforeUpdate);

        if (updatedHandler != null) {
            updatedHandler.updated(info);
        }
    }

    @SuppressWarnings("unchecked")
    private CollisionResult onCollision(
            CollisionHandler collisionHandler,
            InternalCollidableWrapper other) {
        Class<?> type = collisionHandler.getCollidableClass();
        return type.isAssignableFrom(other.collidableClass)
                ? collisionHandler.collision(other.info)
                : CollisionResult.CONTINUE;
    }

    boolean isHandlingCollisions() {
        return collisionHandlers != null && collisionHandlers.size() > 0;
    }

    CollisionResult handleCollision(InternalCollidableWrapper<?> other) {
        for (int i = 0; i < collisionHandlers.size(); ++i) {
            CollisionResult result = onCollision(collisionHandlers.get(i), other);
            if (result == CollisionResult.ABORT || !collidable.canCollide()) {
                return CollisionResult.ABORT;
            }
        }

        return CollisionResult.CONTINUE;
    }

    void initializeCollisionHandlers() {
        for (int i = 0; i < collisionHandlers.size(); ++i) {
            collisionHandlers.get(i).initialize(info);
        }
    }

    void finishCollisionHandlers() {
        for (int i = 0; i < collisionHandlers.size(); ++i) {
            collisionHandlers.get(i).finish();
        }
    }
}