package com.example.framework.android;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import com.gamelibrary2d.Game;
import com.gamelibrary2d.common.disposal.DefaultDisposer;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.common.functional.Action;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;

public abstract class AbstractGameActivity extends Activity {
    protected final DefaultDisposer disposer = new DefaultDisposer();

    private final GameFactory gameFactory;
    private Android_GameLoop gameLoop;

    protected AbstractGameActivity(Game game) {
        gameFactory = (activity, disposer) -> game;
    }

    protected AbstractGameActivity(GameFactory gameFactory) {
        this.gameFactory = gameFactory;
    }

    @Override
    public void onStop() {
        super.onStop();
        gameLoop.pause();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        gameLoop.resume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
    }

    protected void initialize() {
        gameLoop = new Android_GameLoop();
        Game game = gameFactory.create(this, disposer);
        Android_Window window = new Android_Window(this);
        window.setRenderer(new GLSurfaceViewRenderer(game, window, gameLoop));
        setContentView(window);
    }

    protected interface GameFactory {
        Game create(Activity activity, Disposer disposer);
    }

    private class GLSurfaceViewRenderer implements GLSurfaceView.Renderer {
        private final Game game;
        private final Android_Window window;
        private final Android_GameLoop gameLoop;
        private boolean finished;

        GLSurfaceViewRenderer(Game game, Android_Window window, Android_GameLoop gameLoop) {
            this.game = game;
            this.window = window;
            this.gameLoop = gameLoop;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Android_OpenGL.createInstance(window.getSupportedOpenGLVersion());
            try {
                game.start(window, gameLoop);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {

        }

        @Override
        public void onDrawFrame(GL10 gl) {
            if (!finished) {
                if (gameLoop.isRunning()) {
                    gameLoop.triggerUpdate();
                } else {
                    try {
                        gameLoop.getDisposeAction().perform();
                        Action exitAction = gameLoop.getExitAction();
                        if (exitAction != null) {
                            exitAction.perform();
                        }
                        disposer.dispose();
                    } finally {
                        finished = true;
                        finishAndRemoveTask();
                    }
                }
            }
        }
    }
}
