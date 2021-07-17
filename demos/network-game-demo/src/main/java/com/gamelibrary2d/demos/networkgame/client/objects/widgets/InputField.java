package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.components.widgets.AbstractWidget;
import com.gamelibrary2d.components.widgets.TextField;
import com.gamelibrary2d.demos.networkgame.client.resources.Fonts;
import com.gamelibrary2d.demos.networkgame.client.resources.Surfaces;
import com.gamelibrary2d.demos.networkgame.client.resources.Textures;
import com.gamelibrary2d.demos.networkgame.client.settings.Colors;
import com.gamelibrary2d.renderers.ContentRenderer;
import com.gamelibrary2d.renderers.SurfaceRenderer;
import com.gamelibrary2d.resources.HorizontalTextAlignment;
import com.gamelibrary2d.resources.VerticalTextAlignment;

public class InputField extends AbstractWidget<TextField> {
    private final ContentRenderer background;

    public InputField(String text) {
        background = new SurfaceRenderer<>(
                Surfaces.inputField(),
                Textures.inputField());

        background.setColor(Colors.INPUT_FIELD_COLOR);

        TextField textField = new TextField(Fonts.inputField(), text);
        textField.setAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.CENTER);
        setContent(textField);

        setBounds(background.getBounds());
    }

    public int getIntValue() {
        return Integer.parseInt(getContent().getText());
    }

    public String getStringValue() {
        return getContent().getText();
    }

    @Override
    protected void onRender(float alpha) {
        background.render(alpha);
        super.onRender(alpha);
    }

}
