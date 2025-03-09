package com.gamelibrary2d.tools.particlegenerator.panels;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.containers.AbstractPanel;
import com.gamelibrary2d.text.Font;
import com.gamelibrary2d.text.HorizontalTextAlignment;
import com.gamelibrary2d.text.Label;
import com.gamelibrary2d.text.VerticalTextAlignment;
import com.gamelibrary2d.tools.particlegenerator.models.ParticleSystemModel;
import com.gamelibrary2d.tools.particlegenerator.resources.Fonts;
import com.gamelibrary2d.tools.particlegenerator.widgets.Button;
import com.gamelibrary2d.tools.particlegenerator.widgets.ToggleButton;

public class EmitterPanel extends AbstractPanel<GameObject> {
    private final ToggleButton emitSequentialButton;

    public EmitterPanel(ParticleSystemModel particleSystem) {
        Font font = Fonts.getMenuFont();

        Label emitButtonLabel = new Label(font, "Emit");
        emitButtonLabel.setColor(Color.LIGHT_BLUE);
        emitButtonLabel.setAlignment(HorizontalTextAlignment.RIGHT, VerticalTextAlignment.TOP);

        Button emitButton = new Button(emitButtonLabel, particleSystem::emit);
        emitButton.setBounds(emitButtonLabel.getBounds());
        emitButton.setPosition(0, 0);

        emitSequentialButton = new ToggleButton(font, "Emit Sequential");
        Label emitSequentialLabel = emitSequentialButton.getLabel();
        emitSequentialLabel.setColor(Color.LIGHT_BLUE);
        emitSequentialLabel.setAlignment(HorizontalTextAlignment.RIGHT, VerticalTextAlignment.TOP);

        emitSequentialButton.setBounds(emitSequentialLabel.getBounds());
        emitSequentialButton.setPosition(0, -50);

        Label emitAllContext = new Label(font, "Emit All");
        emitAllContext.setColor(Color.LIGHT_BLUE);
        emitAllContext.setAlignment(HorizontalTextAlignment.RIGHT, VerticalTextAlignment.TOP);

        Button emitAllButton = new Button(emitAllContext, particleSystem::emitAll);
        emitAllButton.setBounds(emitAllContext.getBounds());
        emitAllButton.setPosition(0, -100);

        add(emitButton);
        add(emitSequentialButton);
        add(emitAllButton);
    }

    public boolean isLaunchingSequential() {
        return emitSequentialButton.isToggled();
    }
}