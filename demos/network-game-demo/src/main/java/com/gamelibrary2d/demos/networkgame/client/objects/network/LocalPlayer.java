package com.gamelibrary2d.demos.networkgame.client.objects.network;

import com.gamelibrary2d.demos.networkgame.client.frames.game.GameFrameClient;
import com.gamelibrary2d.demos.networkgame.client.input.Controller;
import com.gamelibrary2d.demos.networkgame.client.input.ControllerFactory;
import com.gamelibrary2d.demos.networkgame.client.input.ControllerInputId;
import com.gamelibrary2d.demos.networkgame.client.objects.network.decoration.RendererMap;
import com.gamelibrary2d.demos.networkgame.common.PlayerAcceleration;
import com.gamelibrary2d.io.DataBuffer;

public class LocalPlayer extends AbstractPlayer {
    private final Controller controller;
    private final PlayerAcceleration playerAcceleration;

    private float acceleration;

    public LocalPlayer(ControllerFactory controllerFactory, byte primaryType, GameFrameClient client, DataBuffer buffer) {
        super(primaryType, client, buffer);

        controller = controllerFactory.create();
        playerAcceleration = new PlayerAcceleration(client, getId());

        controller.addBinding(
                ControllerInputId.UP,
                this::setAcceleration);

        controller.addBinding(
                ControllerInputId.LEFT,
                playerAcceleration::setLeftRotationAcceleration);

        controller.addBinding(
                ControllerInputId.RIGHT,
                playerAcceleration::setRightRotationAcceleration);
    }

    public Controller getController() {
        return controller;
    }

    private void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
        playerAcceleration.setAcceleration(acceleration);
    }

    @Override
    public void setRenderer(RendererMap rendererMap) {
        rendererMap.setRenderer(this);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        controller.update(deltaTime);
        playerAcceleration.updateServer();
    }

    public void rotateTowardsGoal(int goalDirection) {
        playerAcceleration.setGoalRotation(goalDirection);
    }
}
