package com.gamelibrary2d.network.server;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.io.Serializable;
import com.gamelibrary2d.network.Communicator;

import java.io.IOException;
import java.util.List;

public interface Host {
    String getHostName();

    void enableConnections(int port) throws IOException;

    void disableConnections() throws IOException;

    void reinitialize(Communicator communicator);

    DataBuffer getStreamBuffer();

    List<Communicator> getCommunicators();

    default void broadcast(int message) {
        List<Communicator> communicators = getCommunicators();
        for (Communicator communicator : communicators) {
            communicator.getOutgoing().putInt(message);
        }
    }

    default void broadcast(float message) {
        List<Communicator> communicators = getCommunicators();
        for (Communicator communicator : communicators) {
            communicator.getOutgoing().putFloat(message);
        }
    }

    default void broadcast(double message) {
        List<Communicator> communicators = getCommunicators();
        for (Communicator communicator : communicators) {
            communicator.getOutgoing().putDouble(message);
        }
    }

    default void broadcast(byte message) {
        List<Communicator> communicators = getCommunicators();
        for (Communicator communicator : communicators) {
            communicator.getOutgoing().put(message);
        }
    }

    default void broadcast(byte[] message, int off, int len) {
        List<Communicator> communicators = getCommunicators();
        for (Communicator communicator : communicators) {
            communicator.getOutgoing().put(message, off, len);
        }
    }

    default void broadcast(Serializable message) {
        List<Communicator> communicators = getCommunicators();
        for (Communicator communicator : communicators) {
            message.serialize(communicator.getOutgoing());
        }
    }
}
