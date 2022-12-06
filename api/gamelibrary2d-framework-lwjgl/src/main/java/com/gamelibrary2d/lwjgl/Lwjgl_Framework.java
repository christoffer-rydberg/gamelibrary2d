package com.gamelibrary2d.lwjgl;

import com.gamelibrary2d.*;
import com.gamelibrary2d.imaging.ImageReader;
import com.gamelibrary2d.input.Joystick;
import com.gamelibrary2d.input.Keyboard;
import com.gamelibrary2d.input.Mouse;
import com.gamelibrary2d.lwjgl.imaging.Lwjgl_ImageReader;
import com.gamelibrary2d.lwjgl.input.Lwjgl_Joystick;
import com.gamelibrary2d.lwjgl.input.Lwjgl_Keyboard;
import com.gamelibrary2d.lwjgl.input.Lwjgl_Mouse;

public class Lwjgl_Framework implements Framework {
    private final Mouse mouse = new Lwjgl_Mouse();
    private final Keyboard keyboard = new Lwjgl_Keyboard();

    @Override
    public Joystick getJoystick() {
        return Lwjgl_Joystick.instance();
    }

    @Override
    public Keyboard getKeyboard() {
        return keyboard;
    }

    @Override
    public Mouse getMouse() {
        return mouse;
    }

    @Override
    public OpenGL getOpenGL() {
        return Lwjgl_OpenGL.instance();
    }

    @Override
    public GameLoop createDefaultGameLoop() {
        return new Lwjgl_GameLoop();
    }

    @Override
    public ImageReader createDefaultImageReader() {
        return new Lwjgl_ImageReader();
    }
}