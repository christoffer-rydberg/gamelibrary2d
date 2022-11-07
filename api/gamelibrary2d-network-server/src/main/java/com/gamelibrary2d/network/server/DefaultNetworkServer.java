package com.gamelibrary2d.network.server;

import com.gamelibrary2d.common.functional.Func;
import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.NetworkService;
import com.gamelibrary2d.network.common.server.Server;
import com.gamelibrary2d.network.common.server.ServerContext;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializationContext;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;

import java.io.IOException;

public final class DefaultNetworkServer extends AbstractNetworkServer {
    private final ServerContext serverContext;

    public DefaultNetworkServer(String hostname, int port, Func<Server, ServerContext> serverContextFactory) {
        super(hostname, port);
        this.serverContext = serverContextFactory.invoke(this);
    }

    public DefaultNetworkServer(String hostname, int port, NetworkService networkService, Func<Server, ServerContext> serverContextFactory) {
        super(hostname, port, networkService);
        this.serverContext = serverContextFactory.invoke(this);
    }

    @Override
    protected void configureClientAuthentication(CommunicatorInitializer initializer) {
        serverContext.configureClientAuthentication(initializer);
    }

    @Override
    protected boolean acceptConnection(String endpoint) {
        return serverContext.acceptConnection(endpoint);
    }

    @Override
    protected void onConnectionFailed(String endpoint, Exception e) {
        serverContext.onConnectionFailed(endpoint, e);
    }

    @Override
    protected void onClientAuthenticated(CommunicatorInitializationContext context, Communicator communicator) {
        serverContext.onClientAuthenticated(context, communicator);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        serverContext.update(deltaTime);
    }

    @Override
    protected void onConnected(Communicator communicator) {
        serverContext.onConnected(communicator);
    }

    @Override
    protected void initializeClient(CommunicatorInitializer initializer) {
        serverContext.configureClientInitialization(initializer);
    }

    @Override
    protected void onClientInitialized(CommunicatorInitializationContext context, Communicator communicator) {
        serverContext.onClientInitialized(context, communicator);
    }

    @Override
    protected void onDisconnected(Communicator communicator, boolean pending) {
        serverContext.onDisconnected(communicator, pending);
    }

    @Override
    protected void onStart() throws IOException {
        super.onStart();
        serverContext.start();
    }

    @Override
    protected void onStop() throws IOException, InterruptedException {
        serverContext.stop();
        super.onStop();
    }

    @Override
    protected void onMessage(Communicator communicator, DataBuffer buffer) {
        serverContext.onMessage(communicator, buffer);
    }
}