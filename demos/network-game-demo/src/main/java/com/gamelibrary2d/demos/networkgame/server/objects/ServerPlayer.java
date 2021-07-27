package com.gamelibrary2d.demos.networkgame.server.objects;

import com.gamelibrary2d.collision.CollisionDetection;
import com.gamelibrary2d.collision.Obstacle;
import com.gamelibrary2d.collision.handlers.BounceHandler;
import com.gamelibrary2d.collision.handlers.RestrictedAreaHandler;
import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.FloatUtils;
import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.random.RandomInstance;
import com.gamelibrary2d.demos.networkgame.common.ObjectTypes;
import com.gamelibrary2d.demos.networkgame.server.DemoGameLogic;
import com.gamelibrary2d.network.common.Communicator;

public class ServerPlayer extends AbstractDemoServerObject implements Obstacle {
    private final static float MAX_SPEED = 100f;
    private final static float MAX_ACCELERATION = 300f;
    private final static float MAX_ROTATION_ACCELERATION = 180f;
    private final static float NO_GOAL_ROTATION = Float.MAX_VALUE;

    private final DemoGameLogic gameLogic;
    private final Communicator communicator;
    private final Color color;
    private float acceleration;
    private float rotationAcceleration;
    private Point accelerationVector = new Point();
    private float goalRotation = NO_GOAL_ROTATION;

    public ServerPlayer(DemoGameLogic gameLogic, Communicator communicator, Rectangle bounds) {
        super(ObjectTypes.PLAYER);
        this.gameLogic = gameLogic;
        this.communicator = communicator;
        this.setBounds(bounds);
        color = new Color(
                RandomInstance.get().nextFloat(),
                RandomInstance.get().nextFloat(),
                RandomInstance.get().nextFloat()
        );
    }

    private void updateAccelerationVector() {
        accelerationVector.set(0f, acceleration);
        accelerationVector.rotate(getRotation());
    }

    @Override
    public void setRotation(float rotation) {
        super.setRotation(rotation);
        updateAccelerationVector();
    }

    @Override
    public void setSecondaryType(byte secondaryType) {
        super.setSecondaryType(secondaryType);
    }

    @Override
    public void serializeMessage(DataBuffer buffer) {
        boolean isLocal = communicator.getOutgoing() == buffer;
        buffer.putBool(isLocal);
        super.serializeMessage(buffer);
        buffer.putFloat(color.getR());
        buffer.putFloat(color.getG());
        buffer.putFloat(color.getB());
        buffer.putFloat(color.getA());
    }

    private void updateRotation(float deltaTime) {
        if (isRotating()) {
            if (goalRotation != NO_GOAL_ROTATION) {
                float maxDelta = FloatUtils.normalizeDegrees(goalRotation - getRotation());

                float rotationAcceleration;
                if (maxDelta >= 0f) {
                    rotationAcceleration = 1f;
                } else {
                    rotationAcceleration = -1f;
                }

                float delta = rotationAcceleration * MAX_ROTATION_ACCELERATION * deltaTime / (1f + acceleration);
                if (Math.abs(delta) >= Math.abs(maxDelta)) {
                    setRotation(goalRotation);
                    goalRotation = NO_GOAL_ROTATION;
                } else {
                    setRotation(getRotation() + delta);
                }
            } else {
                float delta = rotationAcceleration * MAX_ROTATION_ACCELERATION * deltaTime / (1f + acceleration);
                setRotation(getRotation() + delta);
            }
        }
    }

    private void updateAcceleration(float deltaTime) {
        if (isAccelerating()) {
            Point velocity = getVelocity();
            velocity.add(
                    accelerationVector.getX() * MAX_ACCELERATION * deltaTime,
                    accelerationVector.getY() * MAX_ACCELERATION * deltaTime);

            float speed = velocity.getLength();
            if (speed > MAX_SPEED) {
                velocity.multiply(MAX_SPEED / speed);
            }

            onVelocityChanged();
        }
    }

    @Override
    public void update(float deltaTime) {
        updateRotation(deltaTime);
        updateAcceleration(deltaTime);
        super.update(deltaTime);
    }

    public void setSpeedAndDirection(float speed, float direction) {
        super.setSpeedAndDirection(
                Math.min(MAX_SPEED, speed),
                direction);
    }

    @Override
    public float getMass() {
        return 1f;
    }

    @Override
    public void onPushed(Obstacle pusher, float accelerationX, float accelerationY) {
        if (pusher instanceof ServerObstacle) {
            gameLogic.destroy((ServerObstacle) pusher);
            gameLogic.destroy(this);
        } else {
            accelerate(accelerationX, accelerationY);
        }
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = FloatUtils.cap(acceleration, -1, 1);
        updateAccelerationVector();
    }

    public void setRotationAcceleration(float rotationAcceleration) {
        this.goalRotation = NO_GOAL_ROTATION;
        this.rotationAcceleration = FloatUtils.cap(rotationAcceleration, -1, 1);
    }

    public void setGoalRotation(float goalRotation) {
        this.rotationAcceleration = 0f;
        this.goalRotation = goalRotation;
    }

    @Override
    public void addCollisionDetection(CollisionDetection collisionDetection) {
        collisionDetection.add(
                this,
                new RestrictedAreaHandler<>(collisionDetection.getBounds(), ServerPlayer::accelerate),
                new BounceHandler<>(Obstacle.class)
        );
    }

    @Override
    public boolean isAccelerating() {
        return acceleration != 0f;
    }

    public boolean isRotating() {
        return rotationAcceleration != 0f || goalRotation != NO_GOAL_ROTATION;
    }


}
