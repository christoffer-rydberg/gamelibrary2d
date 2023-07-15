package com.gamelibrary2d.demos.networkgame.client.frames.menu;

import com.gamelibrary2d.PointerState;
import com.gamelibrary2d.input.Keyboard;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.Window;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.containers.*;
import com.gamelibrary2d.components.denotations.KeyDownAware;
import com.gamelibrary2d.components.denotations.KeyUpAware;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.demos.networkgame.client.DemoGame;
import com.gamelibrary2d.demos.networkgame.client.ResourceManager;
import com.gamelibrary2d.demos.networkgame.client.objects.widgets.Button;
import com.gamelibrary2d.demos.networkgame.client.objects.widgets.ShadowedLabel;
import com.gamelibrary2d.demos.networkgame.client.resources.Fonts;
import com.gamelibrary2d.demos.networkgame.client.resources.Surfaces;
import com.gamelibrary2d.demos.networkgame.client.settings.Colors;
import com.gamelibrary2d.demos.networkgame.client.urls.Images;
import com.gamelibrary2d.demos.networkgame.client.urls.Music;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.DefaultTexture;
import com.gamelibrary2d.opengl.resources.Surface;
import com.gamelibrary2d.opengl.resources.Texture;
import com.gamelibrary2d.sound.MusicPlayer;
import com.gamelibrary2d.sound.SoundPlayer;
import com.gamelibrary2d.text.HorizontalTextAlignment;
import com.gamelibrary2d.text.Label;
import com.gamelibrary2d.text.VerticalTextAlignment;
import com.gamelibrary2d.updates.AbstractUpdate;
import com.gamelibrary2d.updates.IdleUpdate;
import com.gamelibrary2d.updates.OpacityUpdate;
import com.gamelibrary2d.updates.SequentialUpdater;

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
    private Layer<Renderable> menuLayer;
    private GameObject backgroundLayer;

    private boolean menuLayerIsHidden = true;
    private boolean prepared;

    public MenuFrame(DemoGame game, ResourceManager resourceManager,
                     MusicPlayer musicPlayer, SoundPlayer soundPlayer) {
        super(game);
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

    private DefaultLayerGameObject<Renderable> createBackgroundLayer() throws IOException {
        DefaultLayerGameObject<Renderable> backgroundLayer = new DefaultLayerGameObject<>();
        backgroundLayer.setAutoClearing(false);
        backgroundLayer.add(createBackground());
        return backgroundLayer;
    }

    private Layer<Renderable> createMenuLayer() throws IOException {
        GameObject creditsButton = createCreditsButton();
        creditsButton.setPosition(
                19 * game.getWindow().getWidth() / 20f,
                game.getWindow().getHeight() / 20f);

        Layer<Renderable> menuLayer = new DefaultLayer<>();
        menuLayer.setAutoClearing(false);

        gameTitle = GameTitle.create(game.getWindow(), resourceManager, this);
        MenuPanel menuPanel = new MenuPanel(game);

        Panel<GameObject> panel = new DefaultPanel<>();
        panel.setPosition(game.getWindow().getWidth() / 2f, game.getWindow().getHeight());

        float titleOffset = Math.max(0f, (game.getWindow().getHeight() - menuPanel.getBounds().getHeight()) / 4f);
        panel.stack(gameTitle, Panel.StackOrientation.DOWN, titleOffset);

        float menuOffset = Math.max(0f, titleOffset - gameTitle.getBounds().getHeight() / 2f);
        panel.stack(menuPanel, Panel.StackOrientation.DOWN, menuOffset);

        menuLayer.add(panel);
        menuLayer.add(creditsButton);

        return menuLayer;
    }

    public void prepare() throws IOException {
        if (!prepared) {
            backgroundLayer = createBackgroundLayer();
            menuLayer = createMenuLayer();
            credits = new Credits(game.getWindow(), resourceManager);
            prepared = true;
        }
    }

    @Override
    protected void onBegin() {
        try {
            prepare();

            hideCredits();
            add(backgroundLayer);
            add(menuLayer);
            add(credits);
            runIntro();

            musicPlayer.play(Music.MENU, 1f, false, 10f, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runIntro() {
        menuLayer.setEnabled(false);
        menuLayer.setOpacity(0f);
        backgroundLayer.setScale(10f);

        introUpdater.clear();

        introUpdater.add(new IntroZoomUpdate(backgroundLayer));

        introUpdater.add(this::showMenu);
        introUpdater.add(new IdleUpdate(4f));
        introUpdater.add(gameTitle.createIntro());

        startUpdate(introUpdater);
    }

    private void showMenu() {
        if (menuLayerIsHidden) {
            SequentialUpdater updater = new SequentialUpdater();
            updater.add(() -> menuLayer.setEnabled(true));
            updater.add(new OpacityUpdate(4f, menuLayer, 1f));
            startUpdate(updater);
            menuLayerIsHidden = false;
        }
    }

    @Override
    protected void onEnd() {
        musicPlayer.stop(2f);
    }

    @Override
    protected void onDispose() {
        prepared = false;
    }

    private void showCredits() {
        menuLayer.setEnabled(false);
        credits.enable(this::hideCredits);
    }

    private void hideCredits() {
        credits.disable();
        menuLayer.setEnabled(true);
    }

    private Button createCreditsButton() {
        Label label = new Label(Fonts.button(), "Credits");
        label.setColor(Colors.CREDITS_FONT);
        label.setAlignment(HorizontalTextAlignment.RIGHT, VerticalTextAlignment.BOTTOM);
        return new Button(new ShadowedLabel(label, Colors.CREDITS_FONT_SHADOW), label.calculateBounds(), b -> showCredits());
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

    @Override
    protected boolean onPointerDown(PointerState pointerState, int id, int button, float x, float y) {
        if (credits.isEnabled()) {
            hideCredits();
            return true;
        } else if (menuLayerIsHidden) {
            showMenu();
            return true;
        } else {
            return super.onPointerDown(pointerState, id, button, x, y);
        }
    }

    private static class IntroZoomUpdate extends AbstractUpdate {
        private final GameObject obj;
        private final float initialScale;

        private float alpha;

        IntroZoomUpdate(GameObject obj) {
            super(19f);
            this.obj = obj;
            this.initialScale = obj.getScale().getX();
        }

        @Override
        protected void initialize() {

        }

        @Override
        protected void onUpdate(float deltaTime) {
            deltaTime /= getDuration();
            alpha += deltaTime;
            float radius = initialScale - 1;
            float x = -radius * (1f - alpha) + 0 * alpha;
            double y = Math.sqrt(radius * radius - x * x);
            obj.setScale(initialScale - (float) y);
        }
    }
}
