package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.objects.AbstractGameObject;
import com.gamelibrary2d.renderers.TextRenderer;
import com.gamelibrary2d.widgets.Label;

public class TimeLabel extends AbstractGameObject<Label> {

    public TimeLabel(TextRenderer textRenderer) {
        setContent(new Label(textRenderer));
        setTime(0, 0);
    }

    public void setTimeFromSeconds(int seconds) {
        int min = seconds / 60;
        int sec = seconds - min * 60;
        setTime(min, sec);
    }

    public void setTime(int min, int sec) {
        getContent().setText(String.format("%02d:%02d", min, sec));
    }
}