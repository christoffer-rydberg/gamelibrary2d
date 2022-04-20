package com.gamelibrary2d.network;

import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.components.frames.FrameInitializationContext;
import com.gamelibrary2d.components.frames.FrameInitializer;
import com.gamelibrary2d.network.common.client.Client;
import com.gamelibrary2d.network.common.exceptions.NetworkAuthenticationException;
import com.gamelibrary2d.network.common.exceptions.NetworkConnectionException;
import com.gamelibrary2d.network.common.exceptions.NetworkInitializationException;
import com.gamelibrary2d.network.common.initialization.CommunicationContext;

public abstract class AbstractNetworkFrame<T extends Client> extends AbstractFrame {
    private final Object clientContextKey = new Object();

    private T client;

    protected AbstractNetworkFrame(Disposer disposer) {
        super(disposer);
    }

    protected AbstractNetworkFrame(Disposer disposer, T client) {
        super(disposer);
        this.client = client;
    }

    public T getClient() {
        return client;
    }

    protected void setClient(T client) {
        this.client = client;
    }

    public void disconnect() {
        if (client != null) {
            client.disconnect();
        }
    }

    @Override
    protected void initialize(FrameInitializer initializer) throws Throwable {
        initializer.addTaskAsync(context -> initializeClient(client, context));
        super.initialize(initializer);
        initializer.addTask(context -> client.initialized(context.get(CommunicationContext.class, clientContextKey)));
    }

    private void initializeClient(Client client, FrameInitializationContext context) throws NetworkAuthenticationException, NetworkConnectionException, NetworkInitializationException {
        client.clearInbox();
        CommunicationContext clientContext = client.initialize();
        context.register(clientContextKey, clientContext);
    }

    @Override
    protected final void onUpdate(float deltaTime) {
        client.update(deltaTime, this::onClientUpdate);
    }

    protected void onClientUpdate(float deltaTime) {
        super.onUpdate(deltaTime);
    }
}