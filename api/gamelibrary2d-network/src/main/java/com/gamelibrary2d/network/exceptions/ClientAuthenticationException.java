package com.gamelibrary2d.network.exceptions;

import java.io.IOException;

public class ClientAuthenticationException extends IOException {

    public ClientAuthenticationException() {
    }

    public ClientAuthenticationException(String message) {
        super(message);
    }

    public ClientAuthenticationException(Throwable cause) {
        super(cause);
    }

    public ClientAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}