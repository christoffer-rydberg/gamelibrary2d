package com.gamelibrary2d.demos.networkgame.server;

import com.gamelibrary2d.collision.Collidable;
import com.gamelibrary2d.collision.CollisionDetection;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.demos.networkgame.common.GameSettings;
import com.gamelibrary2d.demos.networkgame.common.ServerMessages;
import com.gamelibrary2d.network.AbstractGameLogic;
import com.gamelibrary2d.network.common.server.Server;

public class DemoGameLogic extends AbstractGameLogic {
    private final Server server;
    private final GameSettings settings;
    private final CollisionDetection<Collidable> collisionDetection;

    public DemoGameLogic(Server server) {
        this.server = server;

        settings = new GameSettings(
                new Rectangle(0, 0, 1600, 1200),
                Rectangle.centered(32f, 32f));

        var center = settings.getGameBounds().center();
        var portal = new Portal(this, center.getX(), center.getY());

        collisionDetection = new CollisionDetection<>(
                settings.getGameBounds(),
                getGameSettings().getBoulderBounds().width() * 4,
                10);

        collisionDetection.add(portal);
    }

    public GameSettings getGameSettings() {
        return settings;
    }

    public void update(float deltaTime) {
        collisionDetection.update(deltaTime);
    }

    public void spawnBoulder(float x, float y) {
        var boulder = new ServerBoulder(settings.getGameBounds(), settings.getBoulderBounds());
        boulder.getPosition().set(x, y);
        register(boulder);
        collisionDetection.add(boulder);
        server.sendToAll(ServerMessages.SPAWN_BOULDER, false);
        server.sendToAll(boulder, false);
    }
}