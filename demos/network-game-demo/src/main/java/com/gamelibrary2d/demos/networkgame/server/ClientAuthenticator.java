package com.gamelibrary2d.demos.networkgame.server;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.io.DynamicByteBuffer;
import com.gamelibrary2d.io.Read;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.initialization.ConnectionContext;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;
import com.gamelibrary2d.network.initialization.UdpConfiguration;
import com.gamelibrary2d.network.Authenticator;
import com.gamelibrary2d.network.server.ServerHandshakeConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

public class ClientAuthenticator implements Authenticator {
    private final KeyPair keyPair;

    public ClientAuthenticator(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    @Override
    public void addAuthentication(ConnectionInitializer initializer) {
        if (keyPair == null) {
            throw new NullPointerException("Key pair has not been set");
        }

        initializer.addProducer((ctx, com) -> log(String.format("%s: Performing client/server handshake", com.getEndpoint())));

        initializer.addConfig(new ServerHandshakeConfiguration(keyPair));
        initializer.addConsumer(this::validatePassword);
        initializer.addConfig(new UdpConfiguration());
    }

    private boolean validatePassword(ConnectionContext context, Communicator communicator, DataBuffer inbox) throws IOException {
        DataBuffer buffer = new DynamicByteBuffer();
        communicator.readEncrypted(inbox, buffer);
        buffer.flip();

        String serverPassword = Read.textWithSizeHeader(buffer, StandardCharsets.UTF_8);
        if (!serverPassword.equals("serverPassword123")) {
            throw new IOException("Wrong password");
        }
        return true;
    }

    private void log(String message) {
        System.out.println(message);
    }
}
