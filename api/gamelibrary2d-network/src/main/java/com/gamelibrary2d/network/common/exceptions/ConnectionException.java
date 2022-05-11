package com.gamelibrary2d.network.common.exceptions;

import java.io.IOException;

public class ConnectionException extends IOException {

    public ConnectionException() {
    }

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(Throwable cause) {
        super(cause);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}