package com.gamelibrary2d.demos.networkgame.client.frames.game;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.containers.BasicLayer;
import com.gamelibrary2d.components.containers.DefaultLayerObject;
import com.gamelibrary2d.components.containers.Layer;
import com.gamelibrary2d.components.denotations.Updatable;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.components.frames.FrameInitializationContext;
import com.gamelibrary2d.components.frames.FrameInitializer;
import com.gamelibrary2d.demos.networkgame.client.DemoGame;
import com.gamelibrary2d.demos.networkgame.client.ResourceManager;
import com.gamelibrary2d.demos.networkgame.client.input.ControllerFactory;
import com.gamelibrary2d.demos.networkgame.client.objects.network.ClientObject;
import com.gamelibrary2d.demos.networkgame.client.objects.network.LocalPlayer;
import com.gamelibrary2d.demos.networkgame.client.objects.network.decoration.ContentMap;
import com.gamelibrary2d.demos.networkgame.client.objects.network.decoration.EffectMap;
import com.gamelibrary2d.demos.networkgame.client.objects.network.decoration.SoundMap;
import com.gamelibrary2d.demos.networkgame.client.objects.network.decoration.TextureMap;
import com.gamelibrary2d.demos.networkgame.client.objects.widgets.AccelerationArea;
import com.gamelibrary2d.demos.networkgame.client.objects.widgets.PictureFrame;
import com.gamelibrary2d.demos.networkgame.client.objects.widgets.RotationArea;
import com.gamelibrary2d.demos.networkgame.client.objects.widgets.TimeLabel;
import com.gamelibrary2d.demos.networkgame.client.resources.Fonts;
import com.gamelibrary2d.demos.networkgame.client.resources.Surfaces;
import com.gamelibrary2d.demos.networkgame.client.urls.Images;
import com.gamelibrary2d.demos.networkgame.client.urls.Music;
import com.gamelibrary2d.demos.networkgame.common.GameSettings;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.framework.Window;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.DefaultTexture;
import com.gamelibrary2d.opengl.resources.Quad;
import com.gamelibrary2d.opengl.resources.Texture;
import com.gamelibrary2d.sound.MusicPlayer;
import com.gamelibrary2d.sound.SoundPlayer;
import com.gamelibrary2d.updaters.DurationUpdater;
import com.gamelibrary2d.updaters.InstantUpdater;
import com.gamelibrary2d.updaters.ParallelUpdater;
import com.gamelibrary2d.updaters.SequentialUpdater;
import com.gamelibrary2d.updates.EmptyUpdate;

import java.io.IOException;
import java.io.InputStream;

public class GameFrame extends AbstractFrame {
    private final DemoGame game;
    private final ResourceManager resourceManager;

    private final DefaultLayerObject<Renderable> gameLayer = new DefaultLayerObject<>();
    private final Layer<Renderable> backgroundLayer = new BasicLayer<>();
    private final Layer<Renderable> backgroundEffects = new BasicLayer<>();
    private final Layer<ClientObject> objectLayer = new BasicLayer<>();
    private final Layer<Renderable> foregroundEffects = new BasicLayer<>();
    private final Layer<Renderable> screenLayer = new BasicLayer<>();
    private final Layer<Renderable> controllerLayer = new BasicLayer<>();

    private final SoundMap soundMap;
    private final EffectMap effects;
    private final TextureMap textures;
    private final ContentMap content = new ContentMap();

    private final MusicPlayer musicPlayer;
    private final GameFrameClient client;

    private Texture backgroundTexture;
    private TimeLabel timeLabel;
    private GameSettings gameSettings;
    private Rectangle renderedGameBounds;
    private boolean prepared;
    private boolean initialized;

    public GameFrame(
            DemoGame game,
            ControllerFactory controllerFactory,
            ResourceManager resourceManager,
            MusicPlayer musicPlayer,
            SoundPlayer soundPlayer,
            SoundMap soundMap) {
        super(game);
        this.game = game;
        this.resourceManager = resourceManager;
        this.musicPlayer = musicPlayer;
        this.soundMap = soundMap;
        this.effects = new EffectMap(resourceManager, soundMap, soundPlayer);
        textures = new TextureMap(resourceManager);
        client = new GameFrameClient(this, controllerFactory);
    }

    private DefaultTexture createTexture(InputStream s) throws IOException {
        return DefaultTexture.create(s, this);
    }

    public void prepare() throws IOException {
        if (!prepared) {
            backgroundTexture = resourceManager.load(Images.GAME_BACKGROUND, this::createTexture);

            soundMap.initialize();
            textures.initialize(this);
            effects.initialize(textures, this);

            Window window = game.getWindow();
            timeLabel = new TimeLabel(Fonts.timer());
            timeLabel.setPosition(
                    window.getWidth() / 2f,
                    9 * window.getHeight() / 10f);

            effects.onLoaded(backgroundEffects, foregroundEffects);

            gameLayer.add(backgroundEffects);
            gameLayer.add(objectLayer);
            gameLayer.add(foregroundEffects);

            screenLayer.add(controllerLayer);
            screenLayer.add(timeLabel);

            prepared = true;
        }
    }

    @Override
    protected void onBegin(FrameInitializer initializer) throws IOException {
        prepare();
        initializer.addBackgroundTask(ctx -> client.connect());
    }

    @Override
    protected void onInitializationFailed(Throwable error) {
        error.printStackTrace();
        game.goToMenu();
    }

    @Override
    protected void onInitializationSuccessful(FrameInitializationContext context) {
        add(backgroundLayer);
        add(gameLayer);
        add(screenLayer);

        musicPlayer.play(Music.GAME, 0.5f, true, 10f, false);

        initialized = true;
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (initialized) {
            client.readIncoming();
            super.onUpdate(deltaTime);
            client.sendOutgoing();
        } else {
            super.onUpdate(deltaTime);
        }
    }

    void applySettings(GameSettings gameSettings) {
        this.gameSettings = gameSettings;

        float windowWidth = game.getWindow().getWidth();
        float windowHeight = game.getWindow().getHeight();

        Rectangle gameBounds = gameSettings.getGameBounds();
        float scale = Math.min(windowWidth / gameBounds.getWidth(), windowHeight / gameBounds.getHeight());

        renderedGameBounds = Rectangle
                .create(gameBounds.getWidth(), gameBounds.getHeight()).resize(scale)
                .move(windowWidth / 2f, windowHeight / 2f);

        gameLayer.setScale(scale, scale);
        gameLayer.setPosition(renderedGameBounds.getLowerX(), renderedGameBounds.getLowerY());

        content.initialize(gameSettings, textures, this);

        backgroundLayer.add(createBackground(
                new Rectangle(0, 0, windowWidth, windowHeight),
                renderedGameBounds));
    }

    @Override
    protected void onEnd() {
        musicPlayer.stop(2f);
    }

    @Override
    protected void onDispose() {
        prepared = false;
        initialized = false;
    }

    private Renderable createBackground(Rectangle windowBounds, Rectangle gameBounds) {
        PictureFrame gameFrame = PictureFrame.create(windowBounds, gameBounds, Color.BLACK, this);

        ContentRenderer background = new SurfaceRenderer<>(
                Surfaces.coverArea(
                        gameBounds,
                        backgroundTexture.getWidth(),
                        backgroundTexture.getHeight(),
                        this),
                backgroundTexture);

        return alpha -> {
            background.render(alpha);
            gameFrame.render(alpha);
        };
    }

    void destroy(ClientObject obj) {
        if (obj instanceof LocalPlayer) {
            removeVirtualController(((LocalPlayer) obj));
        }

        objectLayer.remove(obj);
        obj.destroy();
    }

    private void addRotationController(LocalPlayer player) {
        Window window = game.getWindow();

        float leftMargin = renderedGameBounds.getLowerX();

        Rectangle visualizationArea = new Rectangle(
                0,
                0,
                leftMargin,
                window.getHeight());

        Rectangle controllerArea = new Rectangle(
                0,
                0,
                window.getWidth() / 2f,
                window.getHeight());

        RotationArea rotationController = RotationArea.create(
                game,
                controllerArea,
                player);

        SurfaceRenderer<Quad> areaRenderer = new SurfaceRenderer<>(Quad.create(visualizationArea, this));
        areaRenderer.setColor(0, 0, 0, 1f);
        rotationController.addValueChangedListener(value -> areaRenderer.setColor(0, Math.abs(value), 0, 1f));

        controllerLayer.add(areaRenderer);
        controllerLayer.add(rotationController);
    }

    private void addAccelerationController(LocalPlayer player) {
        Window window = game.getWindow();

        float rightMargin = window.getWidth() - renderedGameBounds.getUpperX();

        Rectangle visualizationArea = new Rectangle(
                window.getWidth() - rightMargin,
                0,
                window.getWidth(),
                window.getHeight());

        Rectangle controllerArea = new Rectangle(
                window.getWidth() / 2f,
                0,
                window.getWidth(),
                window.getHeight());

        AccelerationArea accelerationController = AccelerationArea.create(
                controllerArea,
                player);

        SurfaceRenderer<Quad> areaRenderer = new SurfaceRenderer<>(Quad.create(visualizationArea, this));
        areaRenderer.setColor(0, 0, 0, 1f);
        accelerationController.addValueChangedListener(value -> areaRenderer.setColor(0, value, 0, 1f));

        controllerLayer.add(areaRenderer);
        controllerLayer.add(accelerationController);
    }

    private void addVirtualController(LocalPlayer player) {
        addAccelerationController(player);
        addRotationController(player);
    }

    private void removeVirtualController(LocalPlayer player) {
        controllerLayer.clear();
    }

    void spawn(ClientObject obj) {
        if (obj instanceof LocalPlayer) {
            addVirtualController(((LocalPlayer) obj));
        }

        obj.spawn(this);
        obj.addContent(content);
        obj.addEffects(effects);

        objectLayer.add(obj);
    }

    void goToMenu() {
        game.goToMenu();
    }

    public void gameOver() {
        Rectangle gameBounds = gameSettings.getGameBounds();

        ParallelUpdater parallelUpdater = new ParallelUpdater();
        objectLayer.getChildren().stream()
                .map(obj -> new DurationUpdater(
                        2f,
                        true,
                        new SuckedIntoPortalUpdate(obj, gameBounds.getCenterX(), gameBounds.getCenterY())))
                .forEach(parallelUpdater::add);

        SequentialUpdater sequentialUpdater = new SequentialUpdater();
        sequentialUpdater.add(new DurationUpdater(1.5f, new EmptyUpdate()));
        sequentialUpdater.add(parallelUpdater);
        sequentialUpdater.add(new InstantUpdater(dt -> objectLayer.clear()));
        sequentialUpdater.add(new DurationUpdater(5f, new EmptyUpdate()));
        sequentialUpdater.add(new InstantUpdater(dt -> client.requestNewGame()));

        startUpdater(sequentialUpdater);
    }

    public void gameEnded() {
        objectLayer.clear();
        controllerLayer.clear();
        client.requestNewGame();
    }

    public void setTime(int seconds) {
        timeLabel.setTimeFromSeconds(seconds);
    }

    public GameFrameClient getClient() {
        return client;
    }

    private static class SuckedIntoPortalUpdate implements Updatable {
        private final ClientObject target;
        private final float originX;
        private final float originY;
        private final float goalX;
        private final float goalY;

        private float timer;

        SuckedIntoPortalUpdate(ClientObject target, float goalX, float goalY) {
            this.target = target;
            this.originX = target.getPosition().getX();
            this.originY = target.getPosition().getY();
            this.goalX = goalX;
            this.goalY = goalY;
        }

        @Override
        public void update(float deltaTime) {
            timer += deltaTime;
            target.getPosition().lerp(originX, originY, goalX, goalY, Math.min(timer * 2f, 1f));
            target.setScale(1f - timer);
        }
    }
}