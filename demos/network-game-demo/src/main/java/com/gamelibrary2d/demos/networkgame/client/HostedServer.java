package com.gamelibrary2d.demos.networkgame.client;

import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.functional.Func;
import com.gamelibrary2d.network.client.ConnectionFactory;
import com.gamelibrary2d.network.server.Server;
import com.gamelibrary2d.updating.UpdateLoop;
import java.io.IOException;

public class HostedServer {
    private final Server server;
    private final float ups;
    private final Func<Disposer, ConnectionFactory> createConnectionFactory;
    private final UpdateLoop updateLoop = new UpdateLoop();
    private Thread serverThread;

    public HostedServer(Server server, float ups, Func<Disposer, ConnectionFactory> createConnectionFactory) {
        this.server = server;
        this.ups = ups;
        this.createConnectionFactory = createConnectionFactory;
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

    public ConnectionFactory createConnectionFactory(Disposer disposer) {
        return createConnectionFactory.invoke(disposer);
    }
}
