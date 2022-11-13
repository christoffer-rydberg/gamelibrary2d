package com.gamelibrary2d.network.client;

import com.gamelibrary2d.common.functional.ParameterizedAction;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.NetworkService;
import com.gamelibrary2d.network.common.SocketChannelConnectedHandler;
import com.gamelibrary2d.network.common.SocketChannelFailedConnectionHandler;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class RemoteServer implements Connectable {
    private final String host;
    private final int port;
    private final NetworkService networkService;
    private final boolean owningNetworkService;
    private final ArrayList<ParameterizedAction<CommunicatorInitializer>> authentication = new ArrayList<>();

    private RemoteServer(String host, int port, NetworkService networkService, boolean owningNetworkService) {
        this.host = host;
        this.port = port;
        this.networkService = networkService;
        this.owningNetworkService = owningNetworkService;
    }

    public RemoteServer(String host, int port) {
        this(host, port, new NetworkService(), true);
    }

    public RemoteServer(String host, int port, NetworkService networkService) {
        this(host, port, networkService, false);
    }

    public void addAuthentication(ParameterizedAction<CommunicatorInitializer> configureAuthentication) {
        authentication.add(configureAuthentication);
    }

    @Override
    public Future<Communicator> connect() {
        CompletableFuture<Communicator> future = new CompletableFuture<>();

        SocketChannelConnectedHandler onConnected = socketChannel -> {
            InternalNetworkCommunicator communicator = new InternalNetworkCommunicator(
                    host, networkService, owningNetworkService, this::configureAuthentication);
            socketChannel.socket().setTcpNoDelay(true);
            communicator.setSocketChannel(socketChannel);
            networkService.connect(socketChannel, communicator, communicator::onSocketChannelDisconnected);
            future.complete(communicator);
        };

        SocketChannelFailedConnectionHandler onConnectionFailed = (endpoint, error) -> future.completeExceptionally(error);

        boolean networkServiceWasRunning = networkService.isRunning();
        try {
            networkService.start();
            networkService.connect(host, port, onConnected, onConnectionFailed);
        } catch (IOException e) {
            if (!networkServiceWasRunning) {
                try {
                    networkService.stop();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
            future.completeExceptionally(e);
        }

        return future;
    }

    private void configureAuthentication(CommunicatorInitializer initializer) {
        for (ParameterizedAction<CommunicatorInitializer> auth : authentication) {
            auth.perform(initializer);
        }
    }
}
