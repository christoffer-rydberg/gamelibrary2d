package com.gamelibrary2d.demos.networkgame.client.frames.game;

import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.components.frames.Pipeline;
import com.gamelibrary2d.demos.networkgame.client.DemoGame;
import com.gamelibrary2d.demos.networkgame.client.ResourceManager;
import com.gamelibrary2d.demos.networkgame.client.input.ControllerFactory;
import com.gamelibrary2d.demos.networkgame.client.objects.network.decoration.SoundMap;
import com.gamelibrary2d.network.client.ConnectionFactory;
import com.gamelibrary2d.sound.MusicPlayer;
import com.gamelibrary2d.sound.SoundPlayer;

import java.io.IOException;

public final class GameFrame extends AbstractFrame {
    private final GameFrameManager frameManager;
    private final GameFrameClient frameClient;
    private ConnectionFactory connectionFactory;

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

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void prepare() throws IOException {
        frameManager.prepare();
    }

    @Override
    protected void onBegin() {
        startPipeline(this::initializeFrame, (ctx, e) -> frameManager.goToMenu());
    }

    private void initializeFrame(Pipeline pipeline) {
        pipeline.addTask(ctx -> frameManager.prepare());
        connectToServer(frameClient, connectionFactory, pipeline);
        pipeline.addTask(ctx -> frameManager.onInitializationSuccessful());
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
}
