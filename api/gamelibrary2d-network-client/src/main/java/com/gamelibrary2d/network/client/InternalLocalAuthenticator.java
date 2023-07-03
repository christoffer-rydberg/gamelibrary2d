package com.gamelibrary2d.network.client;

import com.gamelibrary2d.network.Authenticator;

class InternalLocalAuthenticator {
    Authenticator clientSide;
    Authenticator serverSide;

    InternalLocalAuthenticator(Authenticator clientSide, Authenticator serverSide) {
        this.clientSide = clientSide;
        this.serverSide = serverSide;
    }
}
