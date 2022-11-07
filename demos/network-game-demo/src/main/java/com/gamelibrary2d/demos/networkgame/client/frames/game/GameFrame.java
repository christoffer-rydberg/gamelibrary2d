package com.gamelibrary2d.demos.networkgame.client.frames.game;

import com.gamelibrary2d.common.functional.Factory;
import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.components.frames.FrameInitializationContext;
import com.gamelibrary2d.demos.networkgame.client.DemoGame;
import com.gamelibrary2d.demos.networkgame.client.ResourceManager;
import com.gamelibrary2d.components.frames.AbstractClientFrame;
import com.gamelibrary2d.components.frames.ClientFrameInitializer;
import com.gamelibrary2d.demos.networkgame.client.input.ControllerFactory;
import com.gamelibrary2d.demos.networkgame.client.objects.network.decoration.SoundMap;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;
import com.gamelibrary2d.sound.MusicPlayer;
import com.gamelibrary2d.sound.SoundPlayer;

import java.io.IOException;
import java.util.concurrent.Future;

public final class GameFrame extends AbstractClientFrame {
    private final GameFrameManager frameManager;
    private final GameFrameClient frameClient;
    private Factory<Future<Communicator>> communicatorFactory;

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

    public void setCommunicatorFactory(Factory<Future<Communicator>> communicatorFactory) {
        this.communicatorFactory = communicatorFactory;
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
    protected Future<Communicator> connectToServer() {
        return communicatorFactory.create();
    }

    @Override
    protected void onInitialize(ClientFrameInitializer initializer) throws IOException {
        frameManager.prepare();
    }

    @Override
    protected void onInitializeClient(CommunicatorInitializer initializer) {
        frameClient.initialize(initializer);
    }

    @Override
    protected void onClientInitialized(Communicator communicator) {
        frameClient.onInitialized(communicator);
    }

    @Override
    protected void onInitializationFailed(Throwable error) {
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
