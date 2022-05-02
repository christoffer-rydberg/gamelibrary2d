package com.gamelibrary2d.demos.networkgame.client;

import com.gamelibrary2d.common.concurrent.NotHandledException;
import com.gamelibrary2d.common.concurrent.ResultHandlingFuture;
import com.gamelibrary2d.common.disposal.Disposable;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.common.functional.Factory;
import com.gamelibrary2d.common.io.Write;
import com.gamelibrary2d.common.updating.UpdateLoop;
import com.gamelibrary2d.demos.networkgame.server.DemoGameServer;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.client.ClientSideCommunicator;
import com.gamelibrary2d.network.common.client.LocalClientSideCommunicator;
import com.gamelibrary2d.network.common.client.TcpConnectionSettings;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializationContext;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;
import com.gamelibrary2d.network.common.initialization.UdpConfiguration;
import com.gamelibrary2d.network.common.security.ClientHandshakeConfiguration;
import com.gamelibrary2d.network.common.server.DefaultLocalServer;
import com.gamelibrary2d.network.common.server.DefaultNetworkServer;
import com.gamelibrary2d.network.common.server.LocalServer;
import com.gamelibrary2d.network.common.server.Server;

import java.io.IOException;
import java.security.KeyPair;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class ServerManager implements Disposable {
    private final KeyPair keyPair;
    private Thread serverThread;

    private ServerManager(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public static ServerManager create(KeyPair keyPair, Disposer disposer) {
        ServerManager sm = new ServerManager(keyPair);
        disposer.registerDisposal(sm);
        return sm;
    }

    public Future<Communicator> hostLocalServer() {
        return startServer(this::createLocalServer);
    }

    public Future<Communicator> hostNetworkServer(String host, int tcpPort) {
        return startServer(() -> createNetworkServer(host, tcpPort));
    }

    public void stopHostedServer() {
        if (serverThread != null) {
            try {
                serverThread.interrupt();
                serverThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Future<Communicator> connectToServer(String host, int tcpPort) {
        return ClientSideCommunicator.connect(
                new TcpConnectionSettings(host, tcpPort),
                this::configureAuthentication);
    }

    private void configureAuthentication(CommunicatorInitializer initializer) {
        initializer.addConfig(new ClientHandshakeConfiguration());
        initializer.addProducer(this::sendPassword);
        initializer.addConfig(new UdpConfiguration());
    }

    private Future<Communicator> connectToLocalServer(LocalServer server) {
        return CompletableFuture.completedFuture(
                LocalClientSideCommunicator.connect(server));
    }

    private ServerResult createLocalServer() {
        LocalServer localServer = new DefaultLocalServer(DemoGameServer::new);
        try {
            localServer.start();
            return new ServerResult(localServer, () -> connectToLocalServer(localServer));
        } catch (IOException e) {
            CompletableFuture<Communicator> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return new ServerResult(localServer, () -> future);
        }
    }

    private ServerResult createNetworkServer(String hostname, int tcpPort) {
        DefaultNetworkServer server = new DefaultNetworkServer(hostname, tcpPort, s -> new DemoGameServer(s, keyPair));

        try {
            server.start();
            server.listenForConnections(true);
            return new ServerResult(server, () -> {
                Future<Communicator> comFuture = connectToServer(hostname, tcpPort);
                return new ResultHandlingFuture<>(
                        comFuture,
                        com -> {
                            com.addDisconnectedListener(e -> stopServer(server));
                            return com;
                        },
                        e -> {
                            stopServer(server);
                            throw new NotHandledException();
                        }
                );
            });
        } catch (IOException e) {
            CompletableFuture<Communicator> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return new ServerResult(server, () -> future);
        }
    }

    private void sendPassword(CommunicatorInitializationContext ctx, Communicator com) throws IOException {
        com.writeEncrypted(b -> Write.textWithSizeHeader("serverPassword123", b));
    }

    private Future<Communicator> startServer(Factory<ServerResult> serverFactory) {
        CompletableFuture<Communicator> futureCommunicator = new CompletableFuture<>();
        serverThread = new Thread(() -> {
            ServerResult serverResult = serverFactory.create();

            try {
                futureCommunicator.complete(serverResult.communicatorFactory.create().get());
            } catch (Exception e) {
                futureCommunicator.completeExceptionally(e);
                stopServer(serverResult.server);
                return;
            }

            new UpdateLoop(serverResult.server::update, DemoGameServer.UPDATES_PER_SECOND).run();

            stopServer(serverResult.server);
        });

        serverThread.start();

        return futureCommunicator;
    }

    private void stopServer(Server server) {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        stopHostedServer();
    }

    private static class ServerResult {
        private final Server server;
        private final Factory<Future<Communicator>> communicatorFactory;

        ServerResult(Server server, Factory<Future<Communicator>> communicatorFactory) {
            this.server = server;
            this.communicatorFactory = communicatorFactory;
        }
    }
}
