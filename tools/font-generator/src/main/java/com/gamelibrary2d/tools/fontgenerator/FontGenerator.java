package com.gamelibrary2d.tools.fontgenerator;

import com.gamelibrary2d.AbstractGame;
import com.gamelibrary2d.lwjgl.GlfwWindow;
import com.gamelibrary2d.lwjgl.Lwjgl_Framework;

import java.io.IOException;

public class FontGenerator extends AbstractGame {
    FontGenerator() {
        super(new Lwjgl_Framework());
    }

    public static void main(String[] args) throws IOException {
        new FontGenerator().start(GlfwWindow.createWindowed("Font Generator", 1280, 900));
    }

    @Override
    protected void onStart() {
        setFrame(new FontFrame(this));
    }

    @Override
    protected void onExit() {

    }
}