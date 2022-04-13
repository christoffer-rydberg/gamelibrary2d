package com.gamelibrary2d.demos.lightning;

import com.gamelibrary2d.Game;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.Updatable;
import com.gamelibrary2d.lightning.DefaultDynamicLightMap;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;
import com.gamelibrary2d.particles.DefaultParticleSystem;

public class Torch extends AbstractGameObject implements PointerMoveAware, Updatable {
    private final static int pointerId = 0;

    private final Game game;
    private final ContentRenderer renderer;
    private final DefaultDynamicLightMap lightMap;
    private float particleSystemTimer;
    private final DefaultParticleSystem particleSystem;

    Torch(Game game, ContentRenderer renderer, DefaultDynamicLightMap lightMap, DefaultParticleSystem particleSystem) {
        this.game = game;
        this.renderer = renderer;
        this.lightMap = lightMap;
        this.particleSystem = particleSystem;
        particleSystem.setUpdateListener((sys, par) -> {
            float alpha = par.getColorA();
            float size = par.getScale() * par.getScale();
            float light = 2 * (alpha + 0.5f) * size;
            lightMap.addInterpolated(par.getPosX() / 32f, par.getPosY() / 32f, light);
            return true;
        });
    }

    @Override
    public boolean pointerMove(int id, float x, float y, float projectedX, float projectedY) {
        if (id == pointerId) {
            setPosition(projectedX, projectedY);
        }

        return false;
    }

    @Override
    public void update(float deltaTime) {
        lightMap.addInterpolated(getPosition().getX() / 32f, getPosition().getY() / 32f, 20);
        particleSystemTimer = particleSystem.emit(getPosition(), particleSystemTimer + deltaTime);
    }

    @Override
    public Rectangle getBounds() {
        return renderer.getBounds();
    }

    @Override
    protected void onRender(float alpha) {
        if (game.hasPointerFocus(pointerId)) {
            renderer.render(alpha);
        }
    }
}
