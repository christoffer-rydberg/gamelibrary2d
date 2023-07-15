package com.gamelibrary2d.demos.lightning;

import com.gamelibrary2d.Game;
import com.gamelibrary2d.KeyAndPointerState;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.lightning.DefaultDynamicLightMap;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;
import com.gamelibrary2d.particles.DefaultParticleSystem;

public class Torch extends AbstractGameObject implements PointerMoveAware, Updatable {
    private final static int pointerId = 0;
    private final ContentRenderer renderer;
    private final DefaultDynamicLightMap lightMap;
    private float particleSystemTimer;
    private final DefaultParticleSystem particleSystem;
    private final Game game;

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
    public boolean pointerMove(KeyAndPointerState state, int id, float x, float y) {
        if (id == pointerId) {
            setPosition(x, y);
        }

        return false;
    }

    @Override
    public void swallowedPointerMove(KeyAndPointerState state, int id) {

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
