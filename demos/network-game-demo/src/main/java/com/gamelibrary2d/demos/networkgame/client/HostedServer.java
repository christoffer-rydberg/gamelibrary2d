package com.gamelibrary2d.demos.networkgame.client;

import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.client.ConnectionFactory;
import com.gamelibrary2d.network.server.Server;
import com.gamelibrary2d.updating.UpdateLoop;

import java.io.IOException;
import java.util.concurrent.Future;

public class HostedServer implements ConnectionFactory {
    private final Server server;
    private final float ups;
    private final ConnectionFactory connectionFactory;
    private final UpdateLoop updateLoop = new UpdateLoop();
    private Thread serverThread;

    public HostedServer(Server server, ConnectionFactory connectionFactory, float ups) {
        this.server = server;
        this.connectionFactory = connectionFactory;
        this.ups = ups;
    }

    public void start() {
        serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            updateLoop.run(ups, server);

            try {
                server.stop();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        serverThread.start();
    }

    public void stop() {
        if (serverThread != null) {
            try {
                updateLoop.stop();
                serverThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Future<Communicator> createConnection() {
        return connectionFactory.createConnection();
    }
}
