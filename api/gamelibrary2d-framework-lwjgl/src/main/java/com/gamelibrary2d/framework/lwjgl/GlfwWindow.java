package com.gamelibrary2d.framework.lwjgl;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.io.BufferUtils;
import com.gamelibrary2d.framework.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GlfwWindow implements Window {

    public static boolean SETUP_DEBUG_MESSAGE_CALLBACK = false;

    private final boolean fullScreen;
    private final int requestedWidth;
    private final int requestedHeight;
    private final List<WindowHint> windowHints = new ArrayList<>();

    private int actualWidth;
    private int actualHeight;
    private int monitorWidth;
    private int monitorHeight;
    private int physicalWidth;
    private int physicalHeight;

    private long window;
    private long monitor;

    private String title;

    private boolean initialized;

    private MouseCursorMode mouseCursorMode = MouseCursorMode.NORMAL;

    private Callback debugProc;

    /**
     * Last known X coordinate of mouse cursor.
     */
    private float cursorPosX;
    /**
     * Last known Y coordinate of mouse cursor.
     */
    private float cursorPosY;
    private WindowEventListener eventListener;
    private boolean firstEventPoll;

    protected GlfwWindow(String title, int width, int height, boolean fullScreen) {
        this.title = title;
        this.requestedWidth = width;
        this.requestedHeight = height;
        this.fullScreen = fullScreen;
    }

    /**
     * Creates an instance of {@link GlfwWindow} in full-screen mode.
     */
    public static GlfwWindow createFullScreen(String title) {
        return new GlfwWindow(title, -1, -1, true);
    }

    /**
     * Creates an instance of {@link GlfwWindow} in full-screen mode with the
     * specified resolution.
     */
    public static GlfwWindow createFullScreen(String title, int resolutionX, int resolutionY) {
        return new GlfwWindow(title, resolutionX, resolutionY, true);
    }

    /**
     * Creates an instance of {@link GlfwWindow} in windowed mode running at
     * full-screen size with no borders.
     */
    public static GlfwWindow createWindowed(String title) {
        return new GlfwWindow(title, -1, -1, false);
    }

    /**
     * Creates an instance of {@link GlfwWindow} in windowed mode with the
     * specified size.
     *
     * @param width  The window's width in pixels
     * @param height The window's height in pixels
     */
    public static GlfwWindow createWindowed(String title, int width, int height) {
        return new GlfwWindow(title, width, height, false);
    }

    @Override
    public void initialize() {
        if (!initialized) {
            GLFWErrorCallback.createPrint(System.err).set();

            if (!glfwInit()) {
                throw new IllegalStateException("Unable to initialize GLFW");
            }

            initialized = true;

            glfwDefaultWindowHints();

            monitor = glfwGetPrimaryMonitor();
            GLFWVidMode videoMode = glfwGetVideoMode(monitor);
            if (videoMode == null) {
                throw new IllegalStateException("Error getting GLFWVidMode");
            }

            this.monitorWidth = videoMode.width();
            this.monitorHeight = videoMode.height();

            if (fullScreen) {
                int actualWidth = requestedWidth <= 0 ? monitorWidth : Math.min(requestedWidth, monitorWidth);
                int actualHeight = requestedHeight <= 0 ? monitorHeight : Math.min(requestedHeight, monitorHeight);
                onCreate(title, actualWidth, actualHeight, monitor);
            } else {
                boolean isWindowedFullscreen = requestedWidth <= 0;

                if (isWindowedFullscreen) {
                    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
                    glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);

                    boolean isTransparentFrameBuffer = windowHints.stream()
                            .filter(x -> x.hint == GLFW_TRANSPARENT_FRAMEBUFFER).reduce((first, second) -> second)
                            .map(x -> x.value == GLFW_TRUE).orElse(false);

                    if (isTransparentFrameBuffer) {
                        // Transparent buffer does not work in full-screen. Make window smaller:
                        onCreate(title, videoMode.width() - 1, videoMode.height(), NULL);
                    } else {
                        onCreate(title, videoMode.width(), videoMode.height(), NULL);
                    }
                } else {
                    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
                    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
                    onCreate(title, requestedWidth, requestedHeight, NULL);

                    // Center window
                    int xPos = Math.max(0, (videoMode.width() - requestedWidth) / 2);
                    int yPos = Math.max(30, (videoMode.height() - requestedHeight) / 2);
                    glfwSetWindowPos(window, xPos, yPos);
                }
            }

            focus();
        }
    }

    @Override
    public void setEventListener(WindowEventListener eventListener) {
        if (eventListener == null) {
            throw new IllegalStateException("Event listener cannot be null");
        }

        this.firstEventPoll = true;
        this.eventListener = eventListener;

        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                eventListener.onKeyAction(key, getKeyAction(action));
            }
        });

        glfwSetCharCallback(window, new GLFWCharCallback() {
            public void invoke(long window, int charInput) {
                eventListener.onCharInput((char) charInput);
            }
        });

        glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            public void invoke(long window, double posX, double posY) {
                cursorPosX = (float) posX;

                // The GLFW mouse coordinates are relative to the
                // upper left corner of the window with the Y-axis down.
                // The y-value is flipped in order to get the position
                // relative to the lower left corner, with the Y-axis up.
                cursorPosY = (float) (actualHeight - posY);

                eventListener.onPointerMove(0, cursorPosX, cursorPosY);
            }
        });

        glfwSetCursorEnterCallback(window, new GLFWCursorEnterCallback() {
            public void invoke(long window, boolean entered) {
                if (entered) {
                    eventListener.onPointerEnter(0);
                } else {
                    eventListener.onPointerLeave(0);
                }
            }
        });

        glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            public void invoke(long window, int button, int action, int mods) {
                eventListener.onPointerAction(0, button, cursorPosX, cursorPosY, getPointerAction(action));
            }
        });

        glfwSetScrollCallback(window, new GLFWScrollCallback() {
            public void invoke(long window, double xOffset, double yOffset) {
                eventListener.onScroll(0, (float) xOffset, (float) yOffset);
            }
        });

        Lwjgl_Joystick.instance().initialize();

        glfwSetJoystickCallback(new GLFWJoystickCallback() {
            public void invoke(int jid, int event) {
                Lwjgl_Joystick joystick = Lwjgl_Joystick.instance();
                if (event == GLFW_CONNECTED)
                    joystick.onConnected(jid);
                else if (event == GLFW_DISCONNECTED)
                    joystick.onDisconnected(jid);
            }
        });
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
        if (initialized) {
            glfwSetWindowTitle(window, title);
        }
    }

    @Override
    public boolean isFullScreen() {
        return fullScreen;
    }

    @Override
    public int getWidth() {
        return actualWidth;
    }

    @Override
    public int getHeight() {
        return actualHeight;
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
    public int getMonitorWidth() {
        return monitorWidth;
    }

    @Override
    public int getMonitorHeight() {
        return monitorHeight;
    }

    public MouseCursorMode getMouseCursorMode() {
        return mouseCursorMode;
    }

    public void setMouseCursorMode(MouseCursorMode mouseCursorMode) {
        if (this.mouseCursorMode == mouseCursorMode) {
            return;
        }

        if (initialized) {
            setGlfwCursorMode(mouseCursorMode);
        }

        this.mouseCursorMode = mouseCursorMode;
    }

    public void setWindowHint(int hint, int value) {
        if (initialized) {
            throw new RuntimeException("Window has already been initialized");
        }

        windowHints.add(new WindowHint(hint, value));
    }

    private void assertInitialized() {
        if (!initialized) {
            throw new RuntimeException("Window has not been initialized");
        }
    }

    @Override
    public void focus() {
        assertInitialized();
        glfwFocusWindow(window);
    }

    @Override
    public void show() {
        assertInitialized();
        glfwShowWindow(window);
    }

    public void hide() {
        assertInitialized();
        glfwHideWindow(window);
    }

    @Override
    public void render(Color backgroundColor, Renderable content, float alpha) {
        assertInitialized();

        GL11.glClearColor(
                backgroundColor.getR(),
                backgroundColor.getG(),
                backgroundColor.getB(),
                backgroundColor.getA());

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        if (content != null) {
            content.render(alpha);
        }

        glfwSwapBuffers(window);
    }

    @Override
    public void pollEvents() {
        if (firstEventPoll && eventListener != null) {
            double[] posX = new double[1];
            double[] posY = new double[1];
            glfwGetCursorPos(window, posX, posY);
            double cursorPosX = posX[0];
            double cursorPosY = posY[0];

            if (cursorPosX < actualWidth && cursorPosY < actualHeight) {
                this.cursorPosX = (float) cursorPosX;
                this.cursorPosY = (float) (actualHeight - cursorPosY);
                eventListener.onPointerEnter(0);
                eventListener.onPointerMove(0, this.cursorPosX, this.cursorPosY);
            }

            firstEventPoll = false;
        }

        assertInitialized();

        glfwPollEvents();
    }

    @Override
    public boolean isCloseRequested() {
        return initialized && glfwWindowShouldClose(window);
    }

    @Override
    public void dispose() {
        if (initialized) {
            GL.setCapabilities(null);

            if (debugProc != null) {
                debugProc.free();
            }

            if (window != NULL) {
                glfwFreeCallbacks(window);
                glfwDestroyWindow(window);
                window = NULL;
            }

            Objects.requireNonNull(glfwSetJoystickCallback(null)).free();
            Objects.requireNonNull(glfwSetErrorCallback(null)).free();

            glfwTerminate();

            initialized = false;
        }
    }

    private void readActualSize() {
        IntBuffer x = BufferUtils.createIntBuffer(1);
        IntBuffer y = BufferUtils.createIntBuffer(1);
        glfwGetWindowSize(this.window, x, y);

        actualWidth = x.get(0);
        actualHeight = y.get(0);
    }

    private void readPhysicalSize() {
        IntBuffer x = BufferUtils.createIntBuffer(1);
        IntBuffer y = BufferUtils.createIntBuffer(1);
        glfwGetMonitorPhysicalSize(this.monitor, x, y);

        physicalWidth = x.get(0);
        physicalHeight = y.get(0);
    }

    private void onCreate(String title, int width, int height, long monitor) {
        for (WindowHint hint : windowHints) {
            glfwWindowHint(hint.hint, hint.value);
        }

        window = glfwCreateWindow(width, height, title, monitor, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(window);

        setGlfwCursorMode(mouseCursorMode);

        GL.createCapabilities();

        if (SETUP_DEBUG_MESSAGE_CALLBACK)
            debugProc = GLUtil.setupDebugMessageCallback();

        // Enable v-sync
        glfwSwapInterval(1);

        readActualSize();
        readPhysicalSize();
    }

    private void setGlfwCursorMode(MouseCursorMode mouseCursorMode) {
        switch (mouseCursorMode) {
            case DISABLED:
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                break;
            case HIDDEN:
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
                break;
            case NORMAL:
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                break;
        }
    }

    private KeyAction getKeyAction(int action) {
        switch (action) {
            case GLFW_PRESS:
                return KeyAction.DOWN;
            case GLFW_RELEASE:
                return KeyAction.UP;
            case GLFW_REPEAT:
                return KeyAction.DOWN_REPEAT;
            default:
                throw new IllegalStateException("Unexpected value: " + action);
        }
    }

    private PointerAction getPointerAction(int action) {
        switch (action) {
            case GLFW_PRESS:
                return PointerAction.DOWN;
            case GLFW_RELEASE:
                return PointerAction.UP;
            default:
                throw new IllegalStateException("Unexpected value: " + action);
        }
    }

    private static class WindowHint {
        final int hint;
        final int value;

        WindowHint(int hint, int value) {
            this.hint = hint;
            this.value = value;
        }
    }
}