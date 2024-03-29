package com.gamelibrary2d.lwjgl.input;

import com.gamelibrary2d.input.Mouse;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;

public class Lwjgl_Mouse implements Mouse {

    public Lwjgl_Mouse() {
    }

    @Override
    public int mouseButton1() {
        return GLFW.GLFW_MOUSE_BUTTON_1;
    }

    @Override
    public int mouseButton2() {
        return GLFW.GLFW_MOUSE_BUTTON_2;
    }

    @Override
    public boolean isButtonDown(int button) {
        return GLFW.glfwGetMouseButton(glfwGetCurrentContext(), button) == GLFW.GLFW_PRESS;
    }
}