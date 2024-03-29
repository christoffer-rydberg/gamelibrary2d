package com.gamelibrary2d.demos.networkgame.common;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.io.Serializable;

public class GameSettings implements Serializable {
    private final Rectangle gameBounds;
    private final Rectangle obstacleBounds;
    private final Rectangle spaceCraftBounds;

    public GameSettings(Rectangle gameBounds, Rectangle obstacleBounds, Rectangle spaceCraftBounds) {
        this.gameBounds = gameBounds;
        this.obstacleBounds = obstacleBounds;
        this.spaceCraftBounds = spaceCraftBounds;
    }

    public GameSettings(DataBuffer buffer) {
        gameBounds = SerializationUtil.deserializeRectangle(buffer);
        obstacleBounds = SerializationUtil.deserializeRectangle(buffer);
        spaceCraftBounds = SerializationUtil.deserializeRectangle(buffer);
    }

    public Rectangle getGameBounds() {
        return gameBounds;
    }

    public Rectangle getPortalBounds() {
        return obstacleBounds;
    }

    public Rectangle getObstacleBounds() {
        return obstacleBounds;
    }

    public Rectangle getSpaceCraftBounds() {
        return spaceCraftBounds;
    }

    @Override
    public void serialize(DataBuffer buffer) {
        SerializationUtil.serializeRectangle(gameBounds, buffer);
        SerializationUtil.serializeRectangle(obstacleBounds, buffer);
        SerializationUtil.serializeRectangle(spaceCraftBounds, buffer);
    }
}

