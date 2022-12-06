package com.gamelibrary2d.denotations;

import com.gamelibrary2d.Rectangle;

public interface Bounded {
    /**
     * @return The object's bounds unaffected by its position, scale and rotation.
     */
    Rectangle getBounds();
}
