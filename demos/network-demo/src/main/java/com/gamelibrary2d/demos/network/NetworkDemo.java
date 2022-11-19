package com.gamelibrary2d.demos.network;

import com.gamelibrary2d.common.updating.UpdateLoop;
import com.gamelibrary2d.network.client.*;
import com.gamelibrary2d.network.common.exceptions.ClientAuthenticationException;
import com.gamelibrary2d.network.common.exceptions.ClientInitializationException;
import com.gamelibrary2d.network.common.server.Server;
import com.gamelibrary2d.network.common.server.ServerLogic;
import com.gamelibrary2d.network.server.NetworkServer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class NetworkDemo {

    public static void main(String[] args) {
        ServerResult serverResult = (args.length > 0 && args[0].equals("local"))
            ? createLocalServer(new DemoServerLogic())
            : createNetworkServer("localhost", 4444, new DemoServerLogic());

        Thread serverThread = runServer(serverResult.server);
        Thread clientThread = runClient(new DemoClientLogic(), serverResult.connectable);

        try {
            clientThread.join();
            serverThread.interrupt();
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static ServerResult createNetworkServer(String host, int port, ServerLogic serverLogic) {
        System.out.println("Creating network server");
        return new ServerResult(
            new NetworkServer(host, port, serverLogic),
            new RemoteServer(host, port)
        );
    }

    private static ServerResult createLocalServer(ServerLogic logic) {
        System.out.println("Creating local server");
        LocalServer server = new LocalServer(logic);
        return new ServerResult(server, server);
    }

    private static Thread runServer(Server server) {
        Thread thread = new Thread(() -> {
            try {
                server.start();
                server.enableConnections();
                new UpdateLoop().run(10, server);
                server.stop();
            } catch (IOException | InterruptedException e) {
                System.out.println("Failed to start connection server");
                e.printStackTrace();
                System.exit(-1);
            }
        });

        thread.start();

        return thread;
    }

    private static Thread runClient(ClientLogic clientLogic, Connectable server) {
        Thread thread = new Thread(() -> {
            try {
                Client client = new DefaultClient(clientLogic);
                client.initialize(server.connect().get());

                UpdateLoop loop = new UpdateLoop();
                loop.run(10, dt -> {
                    if (client.isConnected()) {
                        client.update(dt);
                    } else {
                        client.update(dt);
                        loop.stop();
                    }
                });
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Failed to connect communicator");
                e.printStackTrace();
            } catch (ClientAuthenticationException e) {
                System.err.println("Failed to authenticate client");
                e.printStackTrace();
            } catch (ClientInitializationException e) {
                System.err.println("Failed to initialize client");
                e.printStackTrace();
            }
        });

        thread.start();

        return thread;
    }

    private static class ServerResult {
        final Server server;
        final Connectable connectable;

        public ServerResult(Server server, Connectable connectable) {
            this.server = server;
            this.connectable = connectable;
        }
    }
}