package com.gamelibrary2d.network.client;

import com.gamelibrary2d.common.functional.ParameterizedAction;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.connections.ConnectionService;
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
    private final ConnectionService connectionService;
    private final boolean ownsConnectionService;
    private final ArrayList<ParameterizedAction<CommunicatorInitializer>> authentication = new ArrayList<>();

    private RemoteServer(String host, int port, ConnectionService connectionService, boolean ownsConnectionService) {
        this.host = host;
        this.port = port;
        this.connectionService = connectionService;
        this.ownsConnectionService = ownsConnectionService;
    }

    public RemoteServer(String host, int port) {
        this(host, port, new ConnectionService(), true);
    }

    public RemoteServer(String host, int port, ConnectionService connectionService) {
        this(host, port, connectionService, false);
    }

    public void addAuthentication(ParameterizedAction<CommunicatorInitializer> configureAuthentication) {
        authentication.add(configureAuthentication);
    }

    @Override
    public Future<Communicator> connect() {
        CompletableFuture<Communicator> future = new CompletableFuture<>();

        SocketChannelConnectedHandler onConnected = socketChannel -> {
            InternalNetworkCommunicator communicator = new InternalNetworkCommunicator(
                    host, connectionService, ownsConnectionService, this::configureAuthentication);
            socketChannel.socket().setTcpNoDelay(true);
            communicator.setSocketChannel(socketChannel);
            connectionService.connect(socketChannel, communicator, communicator::onSocketChannelDisconnected);
            future.complete(communicator);
        };

        SocketChannelFailedConnectionHandler onConnectionFailed = (endpoint, error) -> future.completeExceptionally(error);

        boolean connectionServiceWasRunning = connectionService.isRunning();
        try {
            connectionService.start();
            connectionService.connect(host, port, onConnected, onConnectionFailed);
        } catch (IOException e) {
            if (!connectionServiceWasRunning) {
                try {
                    connectionService.stop();
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
