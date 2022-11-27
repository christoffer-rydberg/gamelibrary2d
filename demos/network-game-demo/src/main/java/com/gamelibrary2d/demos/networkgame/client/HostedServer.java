package com.gamelibrary2d.demos.networkgame.client;

import com.gamelibrary2d.common.updating.UpdateLoop;
import com.gamelibrary2d.network.client.Connectable;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.server.Server;

import java.io.IOException;
import java.util.concurrent.Future;

public class HostedServer implements Connectable {
    private final Server server;
    private final float ups;
    private final Connectable connectable;
    private final UpdateLoop updateLoop = new UpdateLoop();
    private Thread serverThread;

    public HostedServer(Server server, Connectable connect, float ups) {
        this.server = server;
        this.connectable = connect;
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
    public Future<Communicator> connect() {
        return connectable.connect();
    }
}
