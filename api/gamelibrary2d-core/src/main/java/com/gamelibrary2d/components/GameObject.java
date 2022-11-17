package com.gamelibrary2d.components;

import com.gamelibrary2d.common.CoordinateSpace;
import com.gamelibrary2d.common.denotations.Bounded;
import com.gamelibrary2d.components.denotations.Opacifiable;
import com.gamelibrary2d.common.denotations.Transformable;
import com.gamelibrary2d.framework.Renderable;

public interface GameObject extends Renderable, Bounded, Transformable, Opacifiable {

    /**
     * Checks if the object is enabled. Disabled objects are not rendered or updated.
     */
    boolean isEnabled();

    /**
     * Sets if the object is {@link #isEnabled() enabled}.
     */
    void setEnabled(boolean enabled);

    /**
     * Handles rendering of the game object within the game object's {@link CoordinateSpace}.
     */
    Renderable getRenderer();
}
