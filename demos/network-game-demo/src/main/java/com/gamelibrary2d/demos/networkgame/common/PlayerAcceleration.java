package com.gamelibrary2d.demos.networkgame.common;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.io.Serializable;
import com.gamelibrary2d.demos.networkgame.client.frames.game.GameFrameClient;

public class PlayerAcceleration implements Serializable {
    private final GameFrameClient client;
    private final int playerId;

    private float acceleration;
    private float leftAcceleration;
    private float rightAcceleration;
    private int goalRotation = Integer.MAX_VALUE;

    private boolean changed = true;

    public PlayerAcceleration(GameFrameClient client, int playerId) {
        this.client = client;
        this.playerId = playerId;
    }

    public void setAcceleration(float acceleration) {
        if (this.acceleration != acceleration) {
            this.acceleration = acceleration;
            changed = true;
        }
    }

    public void setGoalRotation(int goalRotation) {
        if (this.goalRotation != goalRotation) {
            this.leftAcceleration = 0;
            this.rightAcceleration = 0;
            this.goalRotation = goalRotation;
            changed = true;
        }
    }

    public void setLeftRotationAcceleration(float leftAcceleration) {
        if (this.leftAcceleration != leftAcceleration) {
            this.goalRotation = Integer.MAX_VALUE;
            this.leftAcceleration = leftAcceleration;
            changed = true;
        }
    }

    public void setRightRotationAcceleration(float rightAcceleration) {
        if (this.rightAcceleration != rightAcceleration) {
            this.goalRotation = Integer.MAX_VALUE;
            this.rightAcceleration = rightAcceleration;
            changed = true;
        }
    }

    public void updateServer() {
        if (changed) {
            changed = false;
            serialize(client.getCommunicator().getOutgoing());
        }
    }

    @Override
    public void serialize(DataBuffer buffer) {
        buffer.put(ClientMessages.PLAYER_ACCELERATION);
        buffer.putInt(playerId);
        buffer.putFloat(acceleration);
        buffer.putInt(goalRotation);
        if (goalRotation == Integer.MAX_VALUE) {
            buffer.putFloat(rightAcceleration - leftAcceleration);
        }
    }
}