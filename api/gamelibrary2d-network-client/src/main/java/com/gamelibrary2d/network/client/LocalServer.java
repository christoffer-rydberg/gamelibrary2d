package com.gamelibrary2d.network.client;

import com.gamelibrary2d.common.functional.Func;
import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializationContext;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;
import com.gamelibrary2d.network.common.server.AbstractServer;
import com.gamelibrary2d.network.common.server.BroadcastService;
import com.gamelibrary2d.network.common.server.ServerLogic;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class LocalServer extends AbstractServer implements Connectable {
    private final Func<BroadcastService, ServerLogic> serverLogicFactory;
    private final ArrayList<Communicator> clientSideCommunicators = new ArrayList<>();
    private ServerLogic serverLogic;

    public LocalServer(Func<BroadcastService, ServerLogic> serverLogicFactory) {
        this.serverLogicFactory = serverLogicFactory;
    }

    @Override
    public Future<Communicator> connect() {
        InternalLocalCommunicator communicator = new InternalLocalCommunicator();
        clientSideCommunicators.add(communicator);
        addPendingCommunicator(communicator.getServerSideCommunicator());
        return CompletableFuture.completedFuture(communicator);
    }

    @Override
    protected void onStart() {
        serverLogic = serverLogicFactory.invoke(this);
        serverLogic.onStarted();
    }

    @Override
    protected void onStop() {
        for (Communicator com : clientSideCommunicators) {
            com.disconnect();
        }
        serverLogic.onStopped();
    }

    @Override
    protected void onClientAuthenticated(CommunicatorInitializationContext context, Communicator communicator) {
        serverLogic.onClientAuthenticated(context, communicator);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (serverLogic != null) {
            serverLogic.onUpdate(deltaTime);
        }
    }

    @Override
    protected void onConnected(Communicator communicator) {
        serverLogic.onConnected(communicator);
    }

    @Override
    protected void initializeClient(CommunicatorInitializer initializer) {
        serverLogic.onInitializeClient(initializer);
    }

    @Override
    protected void onClientInitialized(CommunicatorInitializationContext context, Communicator communicator) {
        serverLogic.onClientInitialized(context, communicator);
    }

    @Override
    protected void onDisconnected(Communicator communicator, boolean pending) {
        serverLogic.onDisconnected(communicator, pending);
    }

    @Override
    protected void onMessage(Communicator communicator, DataBuffer buffer) {
        serverLogic.onMessage(communicator, buffer);
    }
}
