package com.gamelibrary2d.components.denotations;

public interface Opacifiable {

    /**
     * The object's opacity.
     */
    float getOpacity();

    /**
     * Sets the object's {@link #getOpacity() opacity}.
     */
    void setOpacity(float opacity);

    /**
     * Adds to the object's {@link #getOpacity() opacity}.
     *
     * @param deltaOpacity The opacity to add.
     */
    default void addOpacity(float deltaOpacity) {
        setOpacity(getOpacity() + deltaOpacity);
    }
}
