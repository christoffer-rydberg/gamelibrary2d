package com.gamelibrary2d.network.common.server;

import com.gamelibrary2d.common.denotations.Updatable;

import java.io.IOException;

public interface Server extends Updatable {
    void start() throws IOException;

    void stop() throws IOException, InterruptedException;

    void enableConnections() throws IOException;

    void disableConnections() throws IOException;
}
