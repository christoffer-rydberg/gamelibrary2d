package com.gamelibrary2d.lwjgl;

import com.gamelibrary2d.GameLoop;
import com.gamelibrary2d.Runtime;
import com.gamelibrary2d.Window;
import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.functional.Action;
import com.gamelibrary2d.updating.Timer;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicBoolean;

public class Lwjgl_GameLoop implements GameLoop {
    private final Timer timer = GLFW::glfwGetTime;

    private final AtomicBoolean running = new AtomicBoolean();
    private Updatable target;
    private Action disposeAction;
    private Window window;

    @Override
    public void initialize(Updatable target, Action disposeAction, Window window) {
        this.target = target;
        this.disposeAction = disposeAction;
        this.window = window;
        running.set(true);
    }

    @Override
    public void start(Action onExit) {
        double prevTime = timer.getTime();
        while (running.get() && !window.isCloseRequested()) {
            double startTime = timer.getTime();
            double deltaTime = startTime - prevTime;
            target.update((float) deltaTime);
            prevTime = startTime;
        }

        running.set(false);

        onExit.perform();

        disposeAction.perform();

        Runtime.dispose();
    }

    @Override
    public void stop() {
        running.set(false);
    }
}
