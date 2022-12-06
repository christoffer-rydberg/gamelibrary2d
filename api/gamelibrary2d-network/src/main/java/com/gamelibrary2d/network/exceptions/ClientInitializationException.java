package com.gamelibrary2d.network.exceptions;

import java.io.IOException;

public class ClientInitializationException extends IOException {

    public ClientInitializationException() {
    }

    public ClientInitializationException(String message) {
        super(message);
    }

    public ClientInitializationException(Throwable cause) {
        super(cause);
    }

    public ClientInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}