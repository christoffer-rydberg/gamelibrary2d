package com.gamelibrary2d.demos.networkgame.server;

import com.gamelibrary2d.collision.CollisionDetection;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.random.RandomInstance;
import com.gamelibrary2d.demos.networkgame.common.GameSettings;
import com.gamelibrary2d.demos.networkgame.server.objects.ServerObject;
import com.gamelibrary2d.demos.networkgame.server.objects.ServerPlayer;
import com.gamelibrary2d.demos.networkgame.server.objects.ServerPortal;

import java.util.List;

public class DemoGameLogic {
    private final DemoServerLogic server;
    private final GameSettings settings;
    private final CollisionDetection collisionDetection;

    private boolean gameOver = true;

    public DemoGameLogic(DemoServerLogic server) {
        this.server = server;

        settings = new GameSettings(
                new Rectangle(0, 0, 1200, 900),
                Rectangle.create(32f, 32f),
                Rectangle.create(28f, 45f));

        collisionDetection = new CollisionDetection(
                settings.getGameBounds(),
                getGameSettings().getObstacleBounds().getWidth() * 4,
                10);
    }

    public void startGame(List<ServerPlayer> players) {
        gameOver = false;

        float centerX = settings.getGameBounds().getCenterX();
        float centerY = settings.getGameBounds().getCenterY();
        for (byte i = 0; i < players.size(); ++i) {
            ServerPlayer player = players.get(i);
            player.setPosition(centerX, centerY);
            float initialRotation = RandomInstance.get().nextFloat() * 360f;
            player.setRotation(initialRotation);
            player.setSpeedAndDirection(Float.MAX_VALUE, initialRotation);
            player.setSecondaryType(i);
            spawn(player);
        }

        ServerPortal portal = new ServerPortal(this, settings.getPortalBounds());
        portal.setPosition(centerX, centerY);
        spawn(portal);
    }

    public void endGame() {
        gameOver = true;
        server.endGame();
        collisionDetection.clear();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public GameSettings getGameSettings() {
        return settings;
    }

    public void update(float deltaTime) {
        collisionDetection.update(deltaTime);
    }

    public void spawn(ServerObject obj) {
        obj.setDestroyed(false);
        obj.addCollisionDetection(collisionDetection);
        server.spawn(obj);
    }

    public void destroy(ServerObject obj) {
        if (!obj.isDestroyed()) {
            obj.setDestroyed(true);
            collisionDetection.remove(obj);
            server.destroy(obj);
        }
    }
}