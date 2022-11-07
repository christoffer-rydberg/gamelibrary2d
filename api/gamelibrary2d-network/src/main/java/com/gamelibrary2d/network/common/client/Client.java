package com.gamelibrary2d.network.common.client;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.exceptions.ClientAuthenticationException;
import com.gamelibrary2d.network.common.exceptions.ClientInitializationException;

public interface Client {

    /**
     * Connects the client.
     *
     * @throws ClientAuthenticationException Client authentication failed.
     * @throws ClientInitializationException Client initialization failed.
     */
    void setCommunicator(Communicator communicator) throws ClientAuthenticationException, ClientInitializationException;

    /**
     * @return The client's {@link Communicator}, or null if the client hasn't been {@link #setCommunicator connected}.
     */
    Communicator getCommunicator();

    /**
     * @return The {@link Communicator#getOutgoing() outgoing buffer} of the {@link #getCommunicator client communicator}.
     */
    default DataBuffer getOutgoing() {
        return getCommunicator().getOutgoing();
    }

    /**
     * Disconnects the client.
     */
    void disconnect();

    /**
     * @return True if the client is connected.
     */
    boolean isConnected();

    /**
     * Attempts to read incoming messages from the {@link #getCommunicator client communicator}.
     */
    void readIncoming();

    /**
     * Attempts to send outgoing messages of the {@link #getCommunicator client communicator}.
     */
    void sendOutgoing();
}
