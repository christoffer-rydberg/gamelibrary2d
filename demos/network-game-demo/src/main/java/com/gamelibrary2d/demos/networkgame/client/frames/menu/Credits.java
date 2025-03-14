package com.gamelibrary2d.demos.networkgame.client.frames.menu;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.components.denotations.Disableable;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.Window;
import com.gamelibrary2d.components.DefaultGameObject;
import com.gamelibrary2d.demos.networkgame.client.ResourceManager;
import com.gamelibrary2d.demos.networkgame.client.resources.Fonts;
import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.functional.Action;
import com.gamelibrary2d.io.Read;
import com.gamelibrary2d.text.Label;
import com.gamelibrary2d.updates.AddPositionUpdate;
import com.gamelibrary2d.updates.SequentialUpdater;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class Credits implements Renderable, Updatable, Disableable {
    private final Window window;
    private final SequentialUpdater updater = new SequentialUpdater();
    private final DefaultGameObject<Label> credits;
    private boolean enabled;
    private float speedFactor;

    Credits(Window window, ResourceManager resourceManager) throws IOException {
        this.window = window;
        try (InputStream stream = resourceManager.open("credits.txt")) {
            String text = Read.text(stream, StandardCharsets.UTF_8);
            Label label = new Label(Fonts.button(), text);
            label.setColor(Color.LIGHT_BLUE);
            credits = new DefaultGameObject<>(label);
        }
    }

    void setSpeedFactor(float speedFactor) {
        this.speedFactor = speedFactor;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    void show(Action onFinished) {
        setEnabled(true);
        setSpeedFactor(1f);

        credits.setPosition(window.getWidth() / 2f, 0);
        float height = credits.getRenderer().getBounds().getHeight();
        float windowHeight = window.getHeight();

        updater.clear();
        updater.add(new AddPositionUpdate(60f, credits, 0, height + windowHeight));
        updater.add(onFinished);
    }

    @Override
    public void update(float deltaTime) {
        updater.update(deltaTime * speedFactor);
    }

    @Override
    public void render(float alpha) {
        credits.render(alpha);
    }
}
