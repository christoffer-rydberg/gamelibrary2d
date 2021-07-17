package com.gamelibrary2d.components.widgets;

import com.gamelibrary2d.components.denotations.InputAware;
import com.gamelibrary2d.components.denotations.KeyAware;
import com.gamelibrary2d.framework.Keyboard;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.resources.Font;

public class TextField extends Label implements Renderable, KeyAware, InputAware {

    public TextField() {

    }

    public TextField(Font font) {
        super(font);
    }

    public TextField(Font font, String text) {
        super(font, text);
    }

    public void addChar(char c) {
        setText(getText() + c);
    }

    public void removeLast() {
        String oldText = getText();
        int textLength = oldText.length();
        if (textLength > 0) {
            setText(oldText.substring(0, textLength - 1));
        }
    }

    @Override
    public void charInput(char charInput) {
        addChar(charInput);
    }

    @Override
    public void keyDown(int key, boolean repeat) {
        if (key == Keyboard.instance().keyBackspace()) {
            removeLast();
        }
    }

    @Override
    public void keyUp(int key) {

    }
}