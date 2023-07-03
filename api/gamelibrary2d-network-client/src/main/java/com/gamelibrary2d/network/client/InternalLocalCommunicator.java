package com.gamelibrary2d.network.client;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.AbstractCommunicator;
import com.gamelibrary2d.network.Authenticator;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;

import java.io.IOException;

class InternalLocalCommunicator extends AbstractCommunicator {
    private final LocalServerSideCommunicator serverSideCommunicator;
    private final Authenticator authenticator;

    InternalLocalCommunicator(InternalLocalAuthenticator authenticator) {
        super(1);
        this.authenticator = authenticator != null ? authenticator.clientSide : null;
        serverSideCommunicator = new LocalServerSideCommunicator(
                this,
                authenticator != null ? authenticator.serverSide : null);
    }

    Communicator getServerSideCommunicator() {
        return serverSideCommunicator;
    }

    @Override
    public void addAuthentication(ConnectionInitializer initializer) {
        if (authenticator != null) {
            authenticator.addAuthentication(initializer);
        }
    }

    @Override
    public String getEndpoint() {
        return "localhost";
    }

    @Override
    protected void send(DataBuffer buffer) throws IOException {
        serverSideCommunicator.addIncoming(0, b -> Util.addIncoming(buffer, b));
    }

    @Override
    protected void onDisconnected(Throwable cause) {
        serverSideCommunicator.disconnect();
    }

    private static class LocalServerSideCommunicator extends AbstractCommunicator {
        private final Communicator clientSideCommunicator;
        private final Authenticator authenticator;

        LocalServerSideCommunicator(Communicator clientSideCommunicator, Authenticator authenticator) {
            super(1);
            this.clientSideCommunicator = clientSideCommunicator;
            this.authenticator = authenticator;
        }

        @Override
        public String getEndpoint() {
            return "localhost";
        }

        @Override
        public void addAuthentication(ConnectionInitializer initializer) {
            if (authenticator != null) {
                authenticator.addAuthentication(initializer);
            }
        }

        @Override
        protected void send(DataBuffer buffer) throws IOException {
            clientSideCommunicator.addIncoming(0, b -> Util.addIncoming(buffer, b));
        }

        @Override
        protected void onDisconnected(Throwable cause) {
            clientSideCommunicator.disconnect();
        }
    }

    private static class Util {
        private static int addIncoming(DataBuffer buffer, DataBuffer b) {
            int posBefore = b.position();
            int size = buffer.remaining();
            b.putBool(false);
            b.putInt(size);
            b.put(buffer);
            return b.position() - posBefore;
        }
    }
}