package com.gamelibrary2d.demos.network;

import com.gamelibrary2d.disposal.DefaultDisposer;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.functional.Func;
import com.gamelibrary2d.network.client.*;
import com.gamelibrary2d.network.exceptions.ClientAuthenticationException;
import com.gamelibrary2d.network.exceptions.ClientInitializationException;
import com.gamelibrary2d.network.server.NetworkServer;
import com.gamelibrary2d.network.server.Server;
import com.gamelibrary2d.network.server.ServerLogic;
import com.gamelibrary2d.updating.UpdateLoop;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class NetworkDemo {

    public static void main(String[] args)  {
        try (DefaultDisposer disposer = new DefaultDisposer()) {
            ServerResult serverResult = (args.length > 0 && args[0].equals("local"))
                    ? createLocalServer()
                    : createNetworkServer("localhost", 4444);

            Thread serverThread = runServer(serverResult.server);
            Thread clientThread = runClient(
                    new DemoClientLogic(),
                    serverResult.createConnectionFactory(disposer));

            try {
                clientThread.join();
                serverThread.interrupt();
                serverThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static ServerResult createNetworkServer(String host, int port) {
        ServerLogic serverLogic = new DemoServerLogic(port);
        System.out.println("Creating network server");
        return new ServerResult(
                new NetworkServer(host, serverLogic),
                d -> new NetworkServerConnectionFactory(host, port, d)
        );
    }

    private static ServerResult createLocalServer() {
        ServerLogic logic = new DemoServerLogic();
        System.out.println("Creating local server");
        LocalServer server = new LocalServer(logic);
        return new ServerResult(server, d -> server);
    }

    private static Thread runServer(Server server) {
        Thread thread = new Thread(() -> {
            try {
                server.start();
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

    private static Thread runClient(ClientLogic clientLogic, ConnectionFactory connectionFactory) {
        Thread thread = new Thread(() -> {
            try {
                Client client = new DefaultClient(clientLogic);
                client.initialize(connectionFactory.createConnection().get());

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
        final Func<Disposer, ConnectionFactory> createConnectionFactory;

        public ServerResult(Server server, Func<Disposer, ConnectionFactory> createConnectionFactory) {
            this.server = server;
            this.createConnectionFactory = createConnectionFactory;
        }

        public ConnectionFactory createConnectionFactory(Disposer disposer) {
            return createConnectionFactory.invoke(disposer);
        }
    }
}