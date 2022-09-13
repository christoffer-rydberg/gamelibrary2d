package com.gamelibrary2d.updates;

import com.gamelibrary2d.components.denotations.Opacifiable;

/**
 * Adds the specified opacity to the {@link Opacifiable} target.
 */
public class AddOpacityUpdate extends AbstractUpdate {
    private final Opacifiable target;
    private final float deltaOpacity;

    public AddOpacityUpdate(float duration, Opacifiable target, float deltaOpacity) {
        super(duration);
        this.target = target;
        this.deltaOpacity = deltaOpacity / duration;
    }

    @Override
    protected void initialize() {

    }

    @Override
    protected void onUpdate(float deltaTime) {
        target.addOpacity(deltaOpacity * deltaTime);
    }
}
