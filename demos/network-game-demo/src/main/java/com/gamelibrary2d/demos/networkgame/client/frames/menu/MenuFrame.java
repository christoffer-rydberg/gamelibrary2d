package com.gamelibrary2d.demos.networkgame.client.frames.menu;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.containers.*;
import com.gamelibrary2d.components.denotations.KeyDownAware;
import com.gamelibrary2d.components.denotations.KeyUpAware;
import com.gamelibrary2d.components.denotations.Updatable;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.components.frames.InitializationContext;
import com.gamelibrary2d.components.objects.GameObject;
import com.gamelibrary2d.demos.networkgame.client.DemoGame;
import com.gamelibrary2d.demos.networkgame.client.ResourceManager;
import com.gamelibrary2d.demos.networkgame.client.objects.widgets.Button;
import com.gamelibrary2d.demos.networkgame.client.objects.widgets.ShadowedLabel;
import com.gamelibrary2d.demos.networkgame.client.resources.Fonts;
import com.gamelibrary2d.demos.networkgame.client.resources.Surfaces;
import com.gamelibrary2d.demos.networkgame.client.settings.Colors;
import com.gamelibrary2d.demos.networkgame.client.urls.Images;
import com.gamelibrary2d.demos.networkgame.client.urls.Music;
import com.gamelibrary2d.framework.Keyboard;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.framework.Window;
import com.gamelibrary2d.renderers.Label;
import com.gamelibrary2d.renderers.SurfaceRenderer;
import com.gamelibrary2d.resources.*;
import com.gamelibrary2d.sound.MusicPlayer;
import com.gamelibrary2d.sound.SoundPlayer;
import com.gamelibrary2d.updaters.DurationUpdater;
import com.gamelibrary2d.updaters.InstantUpdater;
import com.gamelibrary2d.updaters.SequentialUpdater;
import com.gamelibrary2d.updates.EmptyUpdate;
import com.gamelibrary2d.updates.OpacityUpdate;

import java.io.IOException;
import java.io.InputStream;

public class MenuFrame extends AbstractFrame implements KeyDownAware, KeyUpAware {
    private final DemoGame game;
    private final ResourceManager resourceManager;
    private final MusicPlayer musicPlayer;
    private final SoundPlayer soundPlayer;
    private final SequentialUpdater introUpdater = new SequentialUpdater();

    private Credits credits;
    private GameTitle gameTitle;
    private Layer<Renderable> foregroundLayer;
    private LayerObject<Renderable> backgroundLayer;

    private boolean foregroundLayerIsHidden = true;

    public MenuFrame(DemoGame game, ResourceManager resourceManager,
                     MusicPlayer musicPlayer, SoundPlayer soundPlayer) {
        this.game = game;
        this.resourceManager = resourceManager;
        this.musicPlayer = musicPlayer;
        this.soundPlayer = soundPlayer;
    }

    private DefaultTexture createTexture(InputStream s) throws IOException {
        return DefaultTexture.create(s, this);
    }

    private Renderable createBackground() throws IOException {
        Texture backgroundTexture = resourceManager.load(Images.MENU_BACKGROUND, this::createTexture);

        Window window = game.getWindow();
        Surface backgroundSurface = Surfaces.coverArea(
                new Rectangle(0, 0, window.getWidth(), window.getHeight()),
                backgroundTexture.getWidth(),
                backgroundTexture.getHeight(),
                this);

        return new SurfaceRenderer<>(backgroundSurface, backgroundTexture);
    }

    private LayerObject<Renderable> createBackgroundLayer() throws IOException {
        DefaultLayerObject<Renderable> backgroundLayer = new DefaultLayerObject<>();
        backgroundLayer.setAutoClearing(false);
        backgroundLayer.add(createBackground());
        return backgroundLayer;
    }

    private Layer<Renderable> createForegroundLayer() throws IOException {
        GameObject creditsButton = createCreditsButton();
        creditsButton.setPosition(
                19 * game.getWindow().getWidth() / 20f,
                game.getWindow().getHeight() / 20f);

        BasicLayer<Renderable> foregroundLayer = new BasicLayer<>();
        foregroundLayer.setAutoClearing(false);

        gameTitle = GameTitle.create(game.getWindow(), resourceManager, this);
        MenuPanel menuPanel = new MenuPanel(game);

        Panel<GameObject> panel = new DefaultPanel<>();
        panel.setPosition(game.getWindow().getWidth() / 2f, game.getWindow().getHeight());

        float titleOffset = Math.max(0f, (game.getWindow().getHeight() - menuPanel.getBounds().getHeight()) / 4f);
        panel.stack(gameTitle, StackOrientation.DOWN, titleOffset);

        float menuOffset = Math.max(0f, titleOffset - gameTitle.getBounds().getHeight() / 2f);
        panel.stack(menuPanel, StackOrientation.DOWN, menuOffset);

        foregroundLayer.add(panel);
        foregroundLayer.add(creditsButton);

        return foregroundLayer;
    }

    @Override
    protected void onInitialize(InitializationContext context) throws IOException {
        backgroundLayer = createBackgroundLayer();
        foregroundLayer = createForegroundLayer();
        credits = new Credits(game.getWindow(), resourceManager);
    }

    @Override
    protected void onLoad(InitializationContext context) {

    }

    @Override
    protected void onLoaded(InitializationContext context) {
        hideCredits();
        add(backgroundLayer);
        add(foregroundLayer);
        add(credits);
        runIntro();
    }

    private void runIntro() {
        foregroundLayer.setEnabled(false);
        foregroundLayer.setOpacity(0f);
        backgroundLayer.setScale(10f);

        introUpdater.clear();

        introUpdater.add(new DurationUpdater(
                19f,
                true,
                new IntroZoomUpdate(backgroundLayer)
        ));

        introUpdater.add(new InstantUpdater(dt -> showMenu()));
        introUpdater.add(new DurationUpdater(4f, new EmptyUpdate()));
        introUpdater.add(gameTitle.createIntro());

        runUpdater(introUpdater);
    }

    private void showMenu() {
        if (foregroundLayerIsHidden) {
            SequentialUpdater updater = new SequentialUpdater();
            updater.add(new InstantUpdater(dt -> foregroundLayer.setEnabled(true)));
            updater.add(new DurationUpdater(
                    4f,
                    true,
                    new OpacityUpdate<>(foregroundLayer, 1f)
            ));
            runUpdater(updater);
            foregroundLayerIsHidden = false;
        }
    }

    @Override
    protected void onBegin() {
        musicPlayer.play(Music.MENU, 1f, false, 10f, false);
    }

    @Override
    protected void onEnd() {
        musicPlayer.stop(2f);
    }

    private void showCredits() {
        foregroundLayer.setEnabled(false);
        credits.enable(this::hideCredits);
    }

    private void hideCredits() {
        credits.disable();
        foregroundLayer.setEnabled(true);
    }

    private Button createCreditsButton() {
        Label label = new Label(Fonts.button(), "Credits");
        label.setColor(Colors.CREDITS_FONT);
        label.setAlignment(HorizontalTextAlignment.RIGHT, VerticalTextAlignment.BOTTOM);
        return new Button(new ShadowedLabel(label, Colors.CREDITS_FONT_SHADOW), label.calculateBounds(), this::showCredits);
    }

    @Override
    public void keyDown(int key, boolean repeat) {
        if (key == Keyboard.instance().keyEscape()) {
            if (credits.isEnabled()) {
                hideCredits();
            } else {
                showMenu();
            }
        } else if (key == Keyboard.instance().keyEnter()) {
            if (credits.isEnabled()) {
                credits.setSpeedFactor(10f);
            }
        }
    }

    @Override
    public void keyUp(int key) {
        if (key == Keyboard.instance().keyEnter()) {
            if (credits.isEnabled()) {
                credits.setSpeedFactor(1f);
            }
        }
    }

    private static class IntroZoomUpdate implements Updatable {
        private final GameObject obj;
        private final float initialScale;

        private float alpha;

        IntroZoomUpdate(GameObject obj) {
            this.obj = obj;
            this.initialScale = obj.getScale().getX();
        }

        @Override
        public void update(float deltaTime) {
            alpha += deltaTime;
            float radius = initialScale - 1;
            float x = -radius * (1f - alpha) + 0 * alpha;
            double y = Math.sqrt(radius * radius - x * x);
            obj.setScale(initialScale - (float) y);
        }
    }
}
