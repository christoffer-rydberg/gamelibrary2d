package com.gamelibrary2d.components;

import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.denotations.Opacifiable;
import com.gamelibrary2d.denotations.Bounded;
import com.gamelibrary2d.denotations.Transformable;

public interface GameObject extends Renderable, Bounded, Transformable, Opacifiable {

    /**
     * Checks if the object is enabled. Disabled objects are not rendered or updated.
     */
    boolean isEnabled();

    /**
     * Sets if the object is {@link #isEnabled() enabled}.
     */
    void setEnabled(boolean enabled);
}
