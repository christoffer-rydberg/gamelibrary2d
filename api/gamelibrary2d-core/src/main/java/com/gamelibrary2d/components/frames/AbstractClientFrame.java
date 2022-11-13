package com.gamelibrary2d.components.frames;

import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.network.client.Connectable;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.client.AbstractClient;
import com.gamelibrary2d.network.client.Client;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;

import java.io.IOException;
import java.util.concurrent.Future;

public abstract class AbstractClientFrame extends AbstractFrame {
    private final Client client = new FrameClient();

    protected AbstractClientFrame(Disposer parentDisposer) {
        super(parentDisposer);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (isInitialized()) {
            client.readIncoming();
            super.onUpdate(deltaTime);
            if (client.isConnected()) {
                client.sendOutgoing();
            }
        } else {
            super.onUpdate(deltaTime);
        }
    }

    @Override
    protected final void onInitialize(FrameInitializer initializer) throws IOException {
        ClientFrameInitializer clientFrameInitializer = new ClientFrameInitializer(
                initializer, client, this::connectToServer, this::onClientInitialized);

        onInitialize(clientFrameInitializer);

        clientFrameInitializer.addClientInitialization(clientFrameInitializer);
    }

    protected abstract Future<Communicator> connectToServer();
    protected abstract void onInitialize(ClientFrameInitializer initializer) throws IOException;
    protected abstract void onInitializeClient(CommunicatorInitializer initializer);
    protected abstract void onClientInitialized(Communicator communicator);
    protected abstract void onMessage(DataBuffer dataBuffer);

    private class FrameClient extends AbstractClient {

        @Override
        protected void initialize(CommunicatorInitializer initializer) {
            onInitializeClient(initializer);
        }

        @Override
        protected void onMessage(DataBuffer buffer) {
            AbstractClientFrame.this.onMessage(buffer);
        }
    }
}
