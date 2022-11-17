package com.gamelibrary2d.network.common.server;

import com.gamelibrary2d.common.io.Serializable;

public interface BroadcastService {

    void send(int message, boolean stream);

    void send(float message, boolean stream);

    void send(double message, boolean stream);

    void send(byte message, boolean stream);

    void send(byte[] message, int off, int len, boolean stream);

    void send(Serializable message, boolean stream);
}
