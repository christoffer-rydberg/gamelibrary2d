package com.gamelibrary2d.network.client;

import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.functional.ParameterizedAction;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.SocketChannelConnectedHandler;
import com.gamelibrary2d.network.SocketChannelFailedConnectionHandler;
import com.gamelibrary2d.network.connections.ConnectionService;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class NetworkServerConnectionFactory implements ConnectionFactory {
    private final String host;
    private final int port;
    private final ConnectionService connectionService = new ConnectionService();
    private final ArrayList<ParameterizedAction<ConnectionInitializer>> authentication = new ArrayList<>();

    public NetworkServerConnectionFactory(String host, int port, Disposer disposer) {
        this.host = host;
        this.port = port;
        disposer.registerDisposal(() -> {
            try {
                this.connectionService.stop();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void addAuthentication(ParameterizedAction<ConnectionInitializer> configureAuthentication) {
        authentication.add(configureAuthentication);
    }

    @Override
    public Future<Communicator> createConnection() {
        CompletableFuture<Communicator> future = new CompletableFuture<>();

        SocketChannelConnectedHandler onConnected = socketChannel -> {
            InternalNetworkCommunicator communicator = new InternalNetworkCommunicator(
                    host, connectionService, this::configureAuthentication);
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

    private void configureAuthentication(ConnectionInitializer initializer) {
        for (ParameterizedAction<ConnectionInitializer> auth : authentication) {
            auth.perform(initializer);
        }
    }
}
