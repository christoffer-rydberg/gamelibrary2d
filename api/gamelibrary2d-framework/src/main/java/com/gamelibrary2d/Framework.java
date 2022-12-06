package com.gamelibrary2d;

import com.gamelibrary2d.imaging.ImageReader;
import com.gamelibrary2d.input.Joystick;
import com.gamelibrary2d.input.Keyboard;
import com.gamelibrary2d.input.Mouse;

public interface Framework {

    Joystick getJoystick();

    Keyboard getKeyboard();

    Mouse getMouse();

    OpenGL getOpenGL();

    GameLoop createDefaultGameLoop();

    ImageReader createDefaultImageReader();
}