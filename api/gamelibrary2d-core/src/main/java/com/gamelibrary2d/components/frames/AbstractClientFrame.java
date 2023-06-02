package com.gamelibrary2d.components.frames;

import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.functional.ParameterizedAction;
import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.client.Client;
import com.gamelibrary2d.network.client.ClientLogic;
import com.gamelibrary2d.network.client.DefaultClient;
import com.gamelibrary2d.network.initialization.CommunicatorInitializer;

import java.io.IOException;

public abstract class AbstractClientFrame extends AbstractFrame {
    private Client client;

    protected AbstractClientFrame(Disposer parentDisposer) {
        super(parentDisposer);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (client != null) {
            client.update(deltaTime);
        } else {
            super.onUpdate(deltaTime);
        }
    }

    @Override
    protected final void onInitialize(FrameInitializer initializer) throws IOException {
        ClientFrameInitializer clientFrameInitializer = new ClientFrameInitializer(
                this::createClient,
                this,
                initializer);

        onInitialize(clientFrameInitializer);
    }

    private Client createClient(ParameterizedAction<CommunicatorInitializer> onInitializeClient) {
        return new DefaultClient(new FrameClientLogic(onInitializeClient), super::onUpdate);
    }

    final void onClientInitialized(Client client) {
        this.client = client;
    }

    protected abstract void onInitialize(ClientFrameInitializer initializer) throws IOException;

    protected abstract void onMessage(DataBuffer dataBuffer);

    private class FrameClientLogic implements ClientLogic {
        private final ParameterizedAction<CommunicatorInitializer> onInitialize;

        private FrameClientLogic(ParameterizedAction<CommunicatorInitializer> onInitialize) {
            this.onInitialize = onInitialize;
        }

        @Override
        public void onInitialize(CommunicatorInitializer initializer) {
            onInitialize.perform(initializer);
        }

        @Override
        public void onMessage(DataBuffer buffer) {
            AbstractClientFrame.this.onMessage(buffer);
        }
    }
}
