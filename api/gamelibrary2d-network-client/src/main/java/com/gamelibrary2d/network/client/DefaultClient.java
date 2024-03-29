package com.gamelibrary2d.network.client;

import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;

public class DefaultClient extends AbstractClient implements Updatable {
    private final ClientLogic clientLogic;
    private final Updatable onUpdate;

    public DefaultClient(ClientLogic clientLogic) {
        this(clientLogic, null);
    }

    public DefaultClient(ClientLogic clientLogic, Updatable onUpdate) {
        this.clientLogic = clientLogic;
        this.onUpdate = onUpdate;
    }

    @Override
    public void update(float deltaTime) {
        readIncoming();
        if (onUpdate != null) {
            onUpdate.update(deltaTime);
        }
        sendOutgoing();
    }

    @Override
    protected void onInitialize(ConnectionInitializer initializer) {
        clientLogic.onInitialize(initializer);
    }

    @Override
    protected void onMessage(DataBuffer buffer) {
        clientLogic.onMessage(buffer);
    }
}
