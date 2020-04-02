package com.gamelibrary2d.tools.particlegenerator.objects;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.objects.AbstractObservableObject;
import com.gamelibrary2d.objects.ComposableObject;
import com.gamelibrary2d.renderable.Label;

public class ToggleButton extends AbstractObservableObject<Label> implements ComposableObject<Label> {
    private boolean toggled;

    public ToggleButton() {
        setContent(new Label());
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
        getContent().setFontColor(toggled ? Color.GREEN : Color.WHITE);
    }

    @Override
    public Label getContent() {
        return super.getContent();
    }

    @Override
    public void setContent(Label content) {
        super.setContent(content);
    }
}