package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.demos.networkgame.client.resources.Fonts;
import com.gamelibrary2d.opengl.ModelMatrix;
import com.gamelibrary2d.text.Font;
import com.gamelibrary2d.text.HorizontalTextAlignment;
import com.gamelibrary2d.text.Label;
import com.gamelibrary2d.text.VerticalTextAlignment;

public class TimeLabel extends AbstractGameObject {

    private final Label label;

    public TimeLabel(Font font) {
        label = new Label(font);
        label.setColor(Color.LAVENDER);
        label.setAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.CENTER);
        setTime(0, 0);
    }

    public void setTimeFromSeconds(int seconds) {
        int min = seconds / 60;
        int sec = seconds - min * 60;
        setTime(min, sec);
    }

    public void setTime(int min, int sec) {
        label.setText(String.format("%02d:%02d", min, sec));
    }

    @Override
    protected void onRender(float alpha) {
        ModelMatrix.instance().pushMatrix();
        try {
            float fontScale = Fonts.getFontScale();
            ModelMatrix.instance().scalef(fontScale, fontScale, 1f);
            label.render(alpha);
        } finally {
            ModelMatrix.instance().popMatrix();
        }
    }

    @Override
    public Rectangle getBounds() {
        return Rectangle.EMPTY;
    }
}
