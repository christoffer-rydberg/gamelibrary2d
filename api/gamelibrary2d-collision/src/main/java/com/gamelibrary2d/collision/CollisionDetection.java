package com.gamelibrary2d.collision;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.collision.handlers.CollisionHandler;
import com.gamelibrary2d.collision.handlers.UpdatedHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Updates {@link Collidable} objects and performs collision detection. A collision is detected when the bounds of two
 * registered {@link Collidable} overlap. {@link CollisionHandler}'s are used to handle collisions and perform more
 * fine-grained collision detection.
 */
public class CollisionDetection {
    private final Rectangle bounds;
    private final InternalQuadTreeNode rootNode;
    private final InternalPool<InternalQuadTreeNode> nodePool = new InternalPool<>();
    private final ArrayList<ActivationArea> activationAreas = new ArrayList<>();
    private final ArrayList<InternalCollidableWrapper<?>> participants;
    private final ArrayList<InternalCollidableWrapper<?>> updateList;
    private final List<Collidable> updated;

    /**
     * Creates a new {@link CollisionDetection} instance.
     *
     * @param bounds       The collision detection bounds.
     * @param minNodeWidth The minimum node width of the collision quad tree.
     * @param nodeCapacity The object capacity of each node. When exceeded, the node will split into child nodes (quadrants).
     */
    public CollisionDetection(Rectangle bounds, float minNodeWidth, int nodeCapacity) {
        this.bounds = bounds;
        participants = new ArrayList<>();
        rootNode = new InternalQuadTreeNode(nodePool);
        updateList = new ArrayList<>();
        updated = new ArrayList<>();
        rootNode.setBounds(bounds.getLowerX(), bounds.getLowerY(), bounds.getUpperX(), bounds.getUpperY());
        rootNode.setMaxDepth((int) Math.round(Math.log(bounds.getWidth() / minNodeWidth) / Math.log(2)));
        rootNode.setCapacity(nodeCapacity);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Registers the object for automatic updating and collision detection.
     */
    public <T extends Collidable> void add(T obj) {
        participants.add(new InternalCollidableWrapper<>(obj, getClass(obj)));
    }

    /**
     * Registers the object for automatic updating and collision detection.
     */
    public <T extends Collidable> void add(T obj, CollisionHandler<T, ?> collisionHandler) {
        participants.add(new InternalCollidableWrapper<>(obj, getClass(obj), collisionHandler));
    }

    /**
     * Registers the object for automatic updating and collision detection.
     */
    public <T extends Collidable> void add(T obj, Collection<CollisionHandler<T, ?>> collisionHandlers) {
        participants.add(new InternalCollidableWrapper<>(obj, getClass(obj), new ArrayList<>(collisionHandlers)));
    }

    /**
     * Registers the object for automatic updating and collision detection.
     */
    public <T extends Collidable> void add(T obj, UpdatedHandler<T> updatedHandler) {
        participants.add(new InternalCollidableWrapper<>(obj, getClass(obj), updatedHandler));
    }

    /**
     * Registers the object for automatic updating and collision detection.
     */
    public <T extends Collidable> void add(T obj,
                                           UpdatedHandler<T> updatedHandler,
                                           CollisionHandler<T, ?> collisionHandler) {
        participants.add(new InternalCollidableWrapper<>(obj, getClass(obj), updatedHandler, collisionHandler));
    }

    /**
     * Registers the object for automatic updating and collision detection.
     */
    public <T extends Collidable> void add(T obj,
                                           UpdatedHandler<T> updatedHandler,
                                           Collection<CollisionHandler<T, ?>> collisionHandlers) {
        participants.add(new InternalCollidableWrapper<>(obj, getClass(obj), updatedHandler, new ArrayList<>(collisionHandlers)));
    }

    private Class<?> getClass(Object obj) {
        return obj.getClass();
    }

    /**
     * Unregisters the object for automatic updating and collision detection.
     *
     * @return True if the object was unregistered, false otherwise.
     */
    public boolean remove(Object obj) {
        for (int i = 0; i < participants.size(); ++i) {
            if (participants.get(i).collidable.equals(obj)) {
                participants.remove(i);
                return true;
            }
        }

        return false;
    }

    /**
     * Clears registered objects.
     */
    public void clear() {
        participants.clear();
    }

    /**
     * @return A modifiable list of restrictive {@link ActivationArea collision detection areas}. It is safe to move the areas
     * as they are repositioned in the collision quad tree each update. Collision detection will only be performed for
     * {@link Collidable} objects inside one or more area. Objects near the edges can be collided with but are not
     * updated. This is to avoid undetected collisions with objects past the edges. If the list of areas is empty,
     * all objects in the quad tree will be updated and checked for collisions.
     */
    public List<ActivationArea> getActivationAreas() {
        return activationAreas;
    }

    /**
     * Updates all registered objects and performs collision detection.
     *
     * @param deltaTime The time since the last update, in seconds.
     * @return A list of updated objects.
     */
    public List<Collidable> update(float deltaTime) {
        updated.clear();
        rootNode.clear();
        nodePool.reset(0);
        updateList.clear();

        if (deltaTime <= 0) {
            return updated;
        }

        for (int i = 0; i < activationAreas.size(); ++i) {
            rootNode.insertActivationArea(activationAreas.get(i));
        }

        if (!activationAreas.isEmpty()) {
            for (int i = 0; i < participants.size(); ++i) {
                InternalCollidableWrapper<?> participant = participants.get(i);
                InsertionResult result = rootNode.insert(participant);
                if (result == InsertionResult.INSERTED_ACTIVE) {
                    updateList.add(participant);
                }
            }
        } else {
            updateList.addAll(participants);
            for (int i = 0; i < updateList.size(); ++i) {
                rootNode.insertWithoutActivation(updateList.get(i));
            }
        }

        for (int i = 0; i < updateList.size(); ++i) {
            InternalCollidableWrapper<?> obj = updateList.get(i);
            rootNode.update(obj, deltaTime);
            updated.add(obj.collidable);
        }

        return updated;
    }
}