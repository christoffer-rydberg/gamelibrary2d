package com.gamelibrary2d.demos.networkgame.client;

import com.gamelibrary2d.common.functional.Action;
import com.gamelibrary2d.common.updating.UpdateLoop;
import com.gamelibrary2d.network.client.Connectable;
import com.gamelibrary2d.network.common.Communicator;
import java.util.concurrent.Future;

public class HostedServer implements Connectable {
    private final Action start;
    private final Action stop;
    private final UpdateLoop updateLoop;
    private final Connectable connectable;
    private Thread serverThread;

    public HostedServer(Action start, Action stop, Connectable connect, UpdateLoop updateLoop) {
        this.start = start;
        this.stop = stop;
        this.connectable = connect;
        this.updateLoop = updateLoop;
    }

    public void start() {
        serverThread = new Thread(() -> {
            start.perform();
            updateLoop.run();
            stop.perform();
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
