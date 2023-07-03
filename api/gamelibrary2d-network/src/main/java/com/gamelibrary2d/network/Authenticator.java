package com.gamelibrary2d.network;

import com.gamelibrary2d.network.initialization.ConnectionInitializer;

public interface Authenticator {
    void addAuthentication(ConnectionInitializer initializer);
}
