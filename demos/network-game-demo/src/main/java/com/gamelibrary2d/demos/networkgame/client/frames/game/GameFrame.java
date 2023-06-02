package com.gamelibrary2d.demos.networkgame.client.frames.game;

import com.gamelibrary2d.components.frames.AbstractClientFrame;
import com.gamelibrary2d.components.frames.ClientFrameInitializer;
import com.gamelibrary2d.components.frames.FrameInitializationContext;
import com.gamelibrary2d.demos.networkgame.client.DemoGame;
import com.gamelibrary2d.demos.networkgame.client.ResourceManager;
import com.gamelibrary2d.demos.networkgame.client.input.ControllerFactory;
import com.gamelibrary2d.demos.networkgame.client.objects.network.decoration.SoundMap;
import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.client.Connectable;
import com.gamelibrary2d.sound.MusicPlayer;
import com.gamelibrary2d.sound.SoundPlayer;

import java.io.IOException;

public final class GameFrame extends AbstractClientFrame {
    private final GameFrameManager frameManager;
    private final GameFrameClient frameClient;
    private Connectable server;

    public GameFrame(
            DemoGame game,
            ControllerFactory controllerFactory,
            ResourceManager resourceManager,
            MusicPlayer musicPlayer,
            SoundPlayer soundPlayer,
            SoundMap soundMap) {
        super(game);
        this.frameManager = new GameFrameManager(game, this, resourceManager, musicPlayer, soundPlayer, soundMap);
        this.frameClient = new GameFrameClient(frameManager, controllerFactory);
    }

    public void setServer(Connectable server) {
        this.server = server;
    }

    public void prepare() throws IOException {
        frameManager.prepare();
    }

    @Override
    protected void onEnd() {
        frameManager.onEnd();
        frameClient.disconnect();
    }

    @Override
    protected void onDispose() {
        frameManager.onDispose();
    }

    @Override
    protected void onInitialize(ClientFrameInitializer initializer) throws IOException {
        frameManager.prepare();

        initializer.initializeClient(
                server::connect,
                frameClient::initialize,
                frameClient::onInitialized);
    }

    @Override
    protected void onInitializationFailed(Throwable error) {
        System.err.println(error);
        frameManager.goToMenu();
    }

    @Override
    protected void onInitializationSuccessful(FrameInitializationContext context) {
        frameManager.onInitializationSuccessful();
    }

    @Override
    protected void onMessage(DataBuffer dataBuffer) {
        frameClient.onMessage(dataBuffer);
    }
}
