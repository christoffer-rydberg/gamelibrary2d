package com.gamelibrary2d;

import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.functional.Action;

public interface GameLoop {

    void initialize(Updatable target, Action disposeAction, Window window);

    void start(Action onExit);

    void stop();
}
