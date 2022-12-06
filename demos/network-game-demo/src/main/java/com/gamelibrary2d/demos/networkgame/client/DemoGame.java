package com.gamelibrary2d.demos.networkgame.client;

import com.gamelibrary2d.AbstractGame;
import com.gamelibrary2d.Framework;
import com.gamelibrary2d.demos.networkgame.client.frames.SplashFrame;
import com.gamelibrary2d.demos.networkgame.client.frames.game.GameFrame;
import com.gamelibrary2d.demos.networkgame.client.frames.menu.MenuFrame;
import com.gamelibrary2d.demos.networkgame.client.input.ControllerFactory;
import com.gamelibrary2d.demos.networkgame.client.objects.network.decoration.SoundMap;
import com.gamelibrary2d.demos.networkgame.client.options.Options;
import com.gamelibrary2d.demos.networkgame.client.resources.Fonts;
import com.gamelibrary2d.demos.networkgame.client.resources.Surfaces;
import com.gamelibrary2d.demos.networkgame.client.resources.Textures;
import com.gamelibrary2d.demos.networkgame.client.settings.Dimensions;
import com.gamelibrary2d.demos.networkgame.client.urls.Music;
import com.gamelibrary2d.network.client.Connectable;
import com.gamelibrary2d.sound.MusicPlayer;
import com.gamelibrary2d.sound.SoundManager;
import com.gamelibrary2d.sound.SoundPlayer;

import java.io.IOException;
import java.io.InputStream;

public class DemoGame extends AbstractGame {
    private final ControllerFactory controllerFactory;
    private final ServerManager serverManager;
    private final SoundManager soundManager;
    private final ResourceManager resourceManager;
    private final Options options = new Options();
    private MenuFrame menuFrame;
    private GameFrame gameFrame;
    private HostedServer hostedServer;

    public DemoGame(Framework framework, ControllerFactory controllerFactory, ResourceManager resourceManager, ServerManager serverManager, SoundManager soundManager) {
        super(framework);
        this.controllerFactory = controllerFactory;
        this.resourceManager = resourceManager;
        this.serverManager = serverManager;
        this.soundManager = soundManager;
    }

    @Override
    protected void onStart() throws IOException {
        showSplashScreen();
        createGlobalResources();
        prepareFrames();
        setFrame(menuFrame);
    }

    private void startGame(Connectable server) {
        gameFrame.setServer(server);
        setFrame(gameFrame);
    }

    public void goToMenu() {
        try {
            setFrame(menuFrame);
        } finally {
            stopHostedServer();
        }
    }

    private void stopHostedServer() {
        if (hostedServer != null) {
            hostedServer.stop();
            hostedServer = null;
        }
    }

    public void startLocalGame() {
        stopHostedServer();
        hostedServer = serverManager.hostLocalServer();
        hostedServer.start();
        startGame(hostedServer);
    }

    public void hostNetworkGame(String host, int port) {
        stopHostedServer();
        hostedServer = serverManager.hostNetworkServer(host, port);
        hostedServer.start();
        startGame(hostedServer);
    }

    public void joinNetworkGame(String host, int port) {
        startGame(serverManager.connectToServer(host, port));
    }

    private void showSplashScreen() {
        SplashFrame splashFrame = new SplashFrame(this, resourceManager);
        setFrame(splashFrame);
        getWindow().show();
        render();
    }

    private void createGlobalResources() throws IOException {
        Dimensions.create(getWindow());
        Fonts.create(resourceManager, this);
        Surfaces.create(this);
        Textures.create(this);
    }

    private void loadSoundBuffer(String resource, String format) throws IOException {
        try (InputStream stream = resourceManager.open(resource)) {
            soundManager.loadBuffer(resource, stream, format);
        }
    }

    private void prepareFrames() throws IOException {
        loadSoundBuffer(Music.MENU, "ogg");
        loadSoundBuffer(Music.GAME, "ogg");

        MusicPlayer musicPlayer = new MusicPlayer(
                this,
                soundManager,
                10);

        SoundPlayer soundPlayer = new SoundPlayer(
                soundManager,
                10);

        menuFrame = new MenuFrame(this, resourceManager, musicPlayer, soundPlayer);
        menuFrame.prepare();

        gameFrame = new GameFrame(this, controllerFactory, resourceManager, musicPlayer, soundPlayer, new SoundMap(soundManager, resourceManager));
        gameFrame.prepare();
    }

    @Override
    protected void onExit() {
        stopHostedServer();
    }

    public Options getOptions() {
        return options;
    }
}