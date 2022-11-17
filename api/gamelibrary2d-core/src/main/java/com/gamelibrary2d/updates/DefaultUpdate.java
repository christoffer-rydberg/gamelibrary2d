package com.gamelibrary2d.updates;

import com.gamelibrary2d.common.functional.Action;
import com.gamelibrary2d.common.denotations.Updatable;

public class DefaultUpdate extends AbstractUpdate {
    private final Action onInitialize;
    private final Updatable onUpdate;

    public DefaultUpdate(float duration, Updatable onUpdate) {
        this(duration, null, onUpdate);
    }

    public DefaultUpdate(float duration, Action onInitialize, Updatable onUpdate) {
        super(duration);
        this.onInitialize = onInitialize;
        this.onUpdate = onUpdate;
    }

    @Override
    protected void initialize() {
        if (onInitialize != null) {
            onInitialize.perform();
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        onUpdate.update(deltaTime);
    }
}