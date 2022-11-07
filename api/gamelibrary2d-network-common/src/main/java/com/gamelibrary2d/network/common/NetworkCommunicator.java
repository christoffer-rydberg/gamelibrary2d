package com.gamelibrary2d.network.common;

import java.io.IOException;

public interface NetworkCommunicator extends Communicator {

    /**
     * Enables UDP communication. Must be called before {@link #connectUdp}
     *
     * @param connectionType The UDP {@link ConnectionType}.
     * @param localPort The local port or zero to  let the system pick up a port.
     * @return The local port that has been bound.
     */
    int enableUdp(ConnectionType connectionType, int localPort) throws IOException;

    /**
     * Connects the communicator to the specified host port. Note that {@link #enableUdp} must be called before this method.
     * @param hostPort The UDP port of the host.
     */
    void connectUdp(int hostPort) throws IOException;

    /**
     * Disables UDP communication for this communicator and closes any existing UDP connection.
     */
    void disableUdp();
}