package com.gamelibrary2d.network.client;

import com.gamelibrary2d.common.denotations.Updatable;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.exceptions.ClientAuthenticationException;
import com.gamelibrary2d.network.common.exceptions.ClientInitializationException;

public interface Client extends Updatable {

    void initialize(Communicator communicator) throws ClientAuthenticationException, ClientInitializationException;

    Communicator getCommunicator();

    default boolean isConnected() {
        Communicator com = getCommunicator();
        return com != null && com.isConnected();
    }

    default boolean disconnect() {
        if (isConnected()) {
            getCommunicator().disconnect();
            return true;
        }

        return false;
    }
}
