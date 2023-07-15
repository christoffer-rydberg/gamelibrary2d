package com.gamelibrary2d.components;

import com.gamelibrary2d.components.denotations.Disableable;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.denotations.Opacifiable;
import com.gamelibrary2d.denotations.Bounded;
import com.gamelibrary2d.denotations.Transformable;

public interface GameObject extends Renderable, Transformable, Bounded, Opacifiable, Disableable {

}
