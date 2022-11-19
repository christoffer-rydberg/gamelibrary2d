package com.gamelibrary2d.components.frames;

import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.network.client.Client;
import com.gamelibrary2d.network.client.ClientLogic;
import com.gamelibrary2d.network.client.DefaultClient;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;
import java.io.IOException;
import java.util.concurrent.Future;

public abstract class AbstractClientFrame extends AbstractFrame {
    private final Client client;

    protected AbstractClientFrame(Disposer parentDisposer) {
        super(parentDisposer);
        client = new DefaultClient(new FrameClientLogic(), super::onUpdate);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (isInitialized()) {
            client.update(deltaTime);
        } else {
            super.onUpdate(deltaTime);
        }
    }

    @Override
    protected final void onInitialize(FrameInitializer initializer) throws IOException {
        ClientFrameInitializer clientFrameInitializer = new ClientFrameInitializer(client, this, initializer);
        onInitialize(clientFrameInitializer);
        clientFrameInitializer.addClientTasks();
    }

    protected abstract Future<Communicator> connectToServer();
    protected abstract void onInitialize(ClientFrameInitializer initializer) throws IOException;
    protected abstract void onInitializeClient(CommunicatorInitializer initializer);
    protected abstract void onClientInitialized(Communicator communicator);
    protected abstract void onMessage(DataBuffer dataBuffer);

    private class FrameClientLogic implements ClientLogic {
        @Override
        public void onInitialize(CommunicatorInitializer initializer) {
            AbstractClientFrame.this.onInitializeClient(initializer);
        }

        @Override
        public void onMessage(DataBuffer buffer) {
            AbstractClientFrame.this.onMessage(buffer);
        }
    }
}
