package com.gamelibrary2d.network.client;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.AbstractCommunicator;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;

import java.io.IOException;

class InternalLocalCommunicator extends AbstractCommunicator {
    private final LocalServerSideCommunicator serverSideCommunicator;

    InternalLocalCommunicator() {
        super(1);
        serverSideCommunicator = new LocalServerSideCommunicator(this);
    }

    Communicator getServerSideCommunicator() {
        return serverSideCommunicator;
    }

    @Override
    public void configureAuthentication(ConnectionInitializer initializer) {

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

        LocalServerSideCommunicator(Communicator clientSideCommunicator) {
            super(1);
            this.clientSideCommunicator = clientSideCommunicator;
        }

        @Override
        public String getEndpoint() {
            return "localhost";
        }

        @Override
        public void configureAuthentication(ConnectionInitializer initializer) {

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