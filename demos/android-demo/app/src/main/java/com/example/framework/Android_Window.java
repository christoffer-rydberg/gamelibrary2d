package com.example.framework;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.OpenGL;
import com.gamelibrary2d.Window;
import com.gamelibrary2d.WindowEventListener;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.input.PointerAction;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;

class Android_Window extends GLSurfaceView implements Window {
    private static final int POINTER_DOWN = 0;
    private static final int POINTER_MOVE = 1;
    private static final int POINTER_UP = 2;
    private final Activity activity;
    private final MotionEventStash motionEventStash = new MotionEventStash(100);
    private final int monitorWidth;
    private final int monitorHeight;
    private final double physicalWidth;
    private final double physicalHeight;

    private WindowEventListener eventListener;

    Android_Window(Activity activity) {
        super(activity);
        this.activity = activity;
        setEGLContextClientVersion(3);

        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        monitorWidth = Math.round(dm.widthPixels);
        monitorHeight = Math.round(dm.heightPixels);
        physicalWidth = (dm.widthPixels / dm.xdpi) * 25.4;
        physicalHeight = (dm.heightPixels / dm.ydpi) * 25.4;
    }

    OpenGL.OpenGLVersion getSupportedOpenGLVersion() {
        int[] vers = new int[2];
        GLES30.glGetIntegerv(GLES30.GL_MAJOR_VERSION, vers, 0);
        GLES30.glGetIntegerv(GLES30.GL_MINOR_VERSION, vers, 1);
        int major = vers[0];
        int minor = vers[1];
        if (major > 3) {
            return OpenGL.OpenGLVersion.OPENGL_ES_3_2;
        } else if (major == 3 && minor == 0) {
            return OpenGL.OpenGLVersion.OPENGL_ES_3;
        } else if (major == 3 && minor == 1) {
            return OpenGL.OpenGLVersion.OPENGL_ES_3_1;
        } else if (major == 3 && minor >= 2) {
            return OpenGL.OpenGLVersion.OPENGL_ES_3_2;
        } else {
            throw new RuntimeException(String.format("Unsupported OpenGL version: %d.%d", major, minor));
        }
    }

    @Override
    public void setTitle(String title) {
        activity.setTitle(title);
    }

    @Override
    public String getTitle() {
        return activity.getTitle().toString();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void show() {

    }

    @Override
    public boolean isCloseRequested() {
        return false;
    }

    @Override
    public void focus() {

    }

    @Override
    public boolean isFullScreen() {
        return true;
    }

    @Override
    public int getMonitorWidth() {
        return monitorWidth;
    }

    @Override
    public int getMonitorHeight() {
        return monitorHeight;
    }

    @Override
    public double getPhysicalWidth() {
        return physicalWidth;
    }

    @Override
    public double getPhysicalHeight() {
        return physicalHeight;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void setEventListener(WindowEventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (eventListener == null) {
            return false;
        }

        motionEventStash.stashEvent(e);
        return true;
    }

    @Override
    public void pollEvents() {
        motionEventStash.pollEvents();
        motionEventStash.triggerEvents(eventListener);
    }

    @Override
    public void render(Color backgroundColor, Renderable content, float alpha) {
        GLES20.glClearColor(
                backgroundColor.getR(),
                backgroundColor.getG(),
                backgroundColor.getB(),
                backgroundColor.getA());

        GLES20.glClear(GL_COLOR_BUFFER_BIT);

        if (content != null) {
            content.render(alpha);
        }
    }

    private class MotionEventStash {
        private final static int stride = 4;

        private final int[] stashedEvents;
        private final int[] polledEvents;

        private int stashPointer = 0;
        private int polledPointer = 0;

        MotionEventStash(int capacity) {
            stashedEvents = new int[stride * capacity];
            polledEvents = new int[stride * capacity];
        }

        private void stashEvent(MotionEvent e, int action, int index) {
            stashedEvents[stashPointer] = action;
            stashedEvents[stashPointer + 1] = e.getPointerId(index);
            stashedEvents[stashPointer + 2] = (int) e.getX(index);
            stashedEvents[stashPointer + 3] = (int) e.getY(index);
            stashPointer += stride;
        }

        synchronized void stashEvent(MotionEvent e) {
            int action = e.getActionMasked();

            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    int count = e.getPointerCount();
                    for (int i = 0; i < count; ++i) {
                        stashEvent(e, POINTER_MOVE, i);
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    stashEvent(e, POINTER_DOWN, e.getActionIndex());
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL: {
                    stashEvent(e, POINTER_UP, e.getActionIndex());
                    break;
                }
            }
        }

        synchronized void pollEvents() {
            System.arraycopy(stashedEvents, 0, polledEvents, polledPointer, stashPointer);
            polledPointer += stashPointer;
            stashPointer = 0;
        }

        private int getAction(int offset) {
            return polledEvents[offset];
        }

        private int getPointerId(int offset) {
            return polledEvents[offset + 1];
        }

        private float getX(int offset) {
            return polledEvents[offset + 2];
        }

        private float getY(int offset) {
            return getHeight() - polledEvents[offset + 3];
        }

        void triggerEvents(WindowEventListener eventListener) {
            for (int i = 0; i < polledPointer; i += stride) {
                switch (getAction(i)) {
                    case POINTER_DOWN:
                        eventListener.onPointerAction(getPointerId(i), 0, getX(i), getY(i), PointerAction.DOWN);
                        break;
                    case POINTER_MOVE:
                        eventListener.onPointerMove(getPointerId(i), getX(i), getY(i));
                        break;
                    case POINTER_UP:
                        eventListener.onPointerAction(getPointerId(i), 0, getX(i), getY(i), PointerAction.UP);
                        break;
                }
            }

            polledPointer = 0;
        }
    }
}