package com.gamelibrary2d.network.server;

import com.gamelibrary2d.denotations.Updatable;

import java.io.IOException;

public interface Server extends Updatable {
    void start() throws IOException;

    void stop() throws IOException, InterruptedException;

    boolean isRunning();
}
