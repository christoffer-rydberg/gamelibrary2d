package com.gamelibrary2d.framework;

import com.gamelibrary2d.common.functional.Action;
import com.gamelibrary2d.common.denotations.Updatable;

public interface GameLoop {

    void initialize(Updatable target, Action disposeAction, Window window);

    void start(Action onExit);

    void stop();
}
