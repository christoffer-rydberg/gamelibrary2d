package com.gamelibrary2d.input;

import com.gamelibrary2d.Runtime;

public interface Mouse {

    static Mouse instance() {
        return Runtime.getFramework().getMouse();
    }

    int mouseButton1();

    int mouseButton2();

    boolean isButtonDown(int button);
}