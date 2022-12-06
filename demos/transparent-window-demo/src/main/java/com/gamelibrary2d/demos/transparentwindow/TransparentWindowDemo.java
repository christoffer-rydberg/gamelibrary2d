package com.gamelibrary2d.demos.transparentwindow;

import com.gamelibrary2d.lwjgl.GlfwWindow;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;

public class TransparentWindowDemo {
    private static final String title = "Transparent Window Demo";

    private static boolean isWindowed(String mode) {
        return mode.equalsIgnoreCase("w") || mode.equalsIgnoreCase("windowed");
    }

    private static boolean isFullscreen(String mode) {
        return mode.equalsIgnoreCase("f") || mode.equalsIgnoreCase("fullscreen");
    }

    private static GlfwWindow createWindow(String[] args) {
        if (args.length == 0) {
            return GlfwWindow.createWindowed(title);
        } else if (args.length == 1) {
            if (isWindowed(args[0])) {
                return GlfwWindow.createWindowed(title);
            } else if (isFullscreen(args[0])) {
                return GlfwWindow.createWindowed(title);
            } else {
                throw new RuntimeException(String.format("Unsupported window mode: %s", args[0]));
            }
        } else if (args.length == 2) {
            int windowWidth = Integer.parseInt(args[0]);
            int windowHeight = Integer.parseInt(args[1]);
            return GlfwWindow.createWindowed(title, windowWidth, windowHeight);
        } else if (args.length == 3) {
            int windowWidth = Integer.parseInt(args[0]);
            int windowHeight = Integer.parseInt(args[1]);
            if (isWindowed(args[2])) {
                return GlfwWindow.createWindowed(title, windowWidth, windowHeight);
            } else if (isFullscreen(args[2])) {
                return GlfwWindow.createFullScreen(title, windowWidth, windowHeight);
            } else {
                throw new RuntimeException(String.format("Unsupported window mode: %s", args[2]));
            }
        } else {
            throw new RuntimeException("Invalid arguments");
        }
    }

    public static void main(String[] args) throws IOException {
        GlfwWindow window = createWindow(args);
        window.setWindowHint(GLFW_DECORATED, GLFW_FALSE);
        window.setWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
        new DemoGame().start(window);
    }
}