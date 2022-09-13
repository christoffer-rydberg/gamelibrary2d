package com.gamelibrary2d.animations;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.denotations.Updatable;
import com.gamelibrary2d.opengl.shaders.ShaderParameter;

public class AnimatedGameObject extends AbstractGameObject implements Updatable {
    private float animationTime;
    private AnimationRenderer renderer;
    private Rectangle bounds;

    public AnimatedGameObject() {

    }

    public AnimatedGameObject(AnimationRenderer renderer) {
        this.renderer = renderer;
    }

    public AnimatedGameObject(AnimationRenderer renderer, Rectangle bounds) {
        this.renderer = renderer;
        this.bounds = bounds;
    }

    public float getAnimationTime() {
        return animationTime;
    }

    public void setAnimationTime(float time) {
        this.animationTime = time;
        if (renderer != null) {
            renderer.setShaderParameter(ShaderParameter.TIME, time);
        }
    }

    @Override
    public final void update(float deltaTime) {
        if (isEnabled()) {
            onUpdate(deltaTime);
        }
    }

    protected void onUpdate(float deltaTime) {
        setAnimationTime(animationTime + deltaTime);
    }

    @Override
    public AnimationRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(AnimationRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public Rectangle getBounds() {
        return bounds != null ? bounds : renderer.getBounds();
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }
}