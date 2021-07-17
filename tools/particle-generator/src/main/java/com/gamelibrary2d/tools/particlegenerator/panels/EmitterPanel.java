package com.gamelibrary2d.tools.particlegenerator.panels;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.components.containers.AbstractPanel;
import com.gamelibrary2d.components.objects.GameObject;
import com.gamelibrary2d.components.widgets.Label;
import com.gamelibrary2d.resources.Font;
import com.gamelibrary2d.resources.HorizontalTextAlignment;
import com.gamelibrary2d.resources.VerticalTextAlignment;
import com.gamelibrary2d.tools.particlegenerator.models.ParticleSystemModel;
import com.gamelibrary2d.tools.particlegenerator.resources.Fonts;
import com.gamelibrary2d.tools.particlegenerator.widgets.Button;
import com.gamelibrary2d.tools.particlegenerator.widgets.ToggleButton;

public class EmitterPanel extends AbstractPanel<GameObject> {
    private final ToggleButton emitSequentialButton;

    public EmitterPanel(ParticleSystemModel particleSystem) {
        Font font = Fonts.getMenuFont();

        Label emitButtonContent = new Label(font, "Emit");
        emitButtonContent.setColor(Color.SOFT_BLUE);
        emitButtonContent.setAlignment(HorizontalTextAlignment.RIGHT, VerticalTextAlignment.TOP);

        Button<Label> emitButton = new Button<>(emitButtonContent, particleSystem::emit);
        emitButton.setBounds(emitButtonContent.calculateBounds());
        emitButton.setPosition(0, 0);

        emitSequentialButton = new ToggleButton(font, "Emit Sequential");
        Label emitSequentialContent = emitSequentialButton.getContent();
        emitSequentialContent.setColor(Color.SOFT_BLUE);
        emitSequentialContent.setAlignment(HorizontalTextAlignment.RIGHT, VerticalTextAlignment.TOP);

        emitSequentialButton.setBounds(emitSequentialContent.calculateBounds());
        emitSequentialButton.setPosition(0, -50);

        Label emitAllContext = new Label(font, "Emit All");
        emitAllContext.setColor(Color.SOFT_BLUE);
        emitAllContext.setAlignment(HorizontalTextAlignment.RIGHT, VerticalTextAlignment.TOP);

        Button<Label> emitAllButton = new Button<>(emitAllContext, particleSystem::emitAll);
        emitAllButton.setBounds(emitAllContext.calculateBounds());
        emitAllButton.setPosition(0, -100);

        add(emitButton);
        add(emitSequentialButton);
        add(emitAllButton);
    }

    public boolean isLaunchingSequential() {
        return emitSequentialButton.isToggled();
    }
}