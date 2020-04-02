package com.gamelibrary2d.framework.lwjgl;

import com.gamelibrary2d.framework.*;

public class Lwjgl_Framework implements Framework {

    @Override
    public Joystick getJoystick() {
        return Lwjgl_Joystick.instance();
    }

    @Override
    public Keyboard getKeyboard() {
        return Lwjgl_Keyboard.instance();
    }

    @Override
    public Mouse getMouse() {
        return Lwjgl_Mouse.instance();
    }

    @Override
    public OpenGL getOpenGL() {
        return Lwjgl_OpenGL.instance();
    }

    @Override
    public Timer createTimer() {
        return new Lwjgl_Timer();
    }
}