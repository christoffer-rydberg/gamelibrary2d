package com.gamelibrary2d.network.common.server;

import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.common.io.Serializable;

import java.io.IOException;

public interface Server {
    void start() throws IOException;

    void stop() throws IOException, InterruptedException;

    boolean isRunning();

    void update(float deltaTime);

    void send(Communicator communicator, int message);

    void send(Communicator communicator, float message);

    void send(Communicator communicator, double message);

    void send(Communicator communicator, byte message);

    void send(Communicator communicator, byte[] message, int off, int len);

    void send(Communicator communicator, Serializable message);

    void sendToAll(int message, boolean stream);

    void sendToAll(float message, boolean stream);

    void sendToAll(double message, boolean stream);

    void sendToAll(byte message, boolean stream);

    void sendToAll(byte[] message, int off, int len, boolean stream);

    void sendToAll(Serializable message, boolean stream);

}