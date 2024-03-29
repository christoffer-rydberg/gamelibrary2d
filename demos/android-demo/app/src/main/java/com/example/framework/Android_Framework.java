package com.example.framework;

import com.gamelibrary2d.*;
import com.gamelibrary2d.imaging.ImageReader;
import com.gamelibrary2d.input.*;

public class Android_Framework implements Framework {

    @Override
    public Joystick getJoystick() {
        return null;
    }

    @Override
    public Keyboard getKeyboard() {
        return null;
    }

    @Override
    public Mouse getMouse() {
        return null;
    }

    @Override
    public OpenGL getOpenGL() {
        return Android_OpenGL.instance();
    }

    @Override
    public GameLoop createDefaultGameLoop() {
        throw new RuntimeException("No default game loop is registered for the Android Framework");
    }

    @Override
    public ImageReader createDefaultImageReader() {
        return new Android_ImageReader();
    }
}
