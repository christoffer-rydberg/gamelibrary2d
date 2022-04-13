package com.gamelibrary2d.demos.networkgame.client.objects.network;

import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.frames.Frame;
import com.gamelibrary2d.demos.networkgame.client.objects.network.decoration.ContentMap;
import com.gamelibrary2d.demos.networkgame.client.objects.network.decoration.EffectMap;

public interface ClientObject extends GameObject {

    int getId();

    byte getPrimaryType();

    byte getSecondaryType();

    Point getParticleHotspot();

    void setGoalPosition(float x, float y);

    void setGoalRotation(float rotation);

    void setGoalDirection(float direction);

    float getDirection();

    void addContent(ContentMap contentMap);

    void addEffects(EffectMap effectMap);

    void destroy();

    void setAccelerating(boolean accelerating);

    void spawn(Frame frame);
}


