package com.gamelibrary2d.network.connections;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.ChannelDisconnectedHandler;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.ConnectionType;
import com.gamelibrary2d.network.DataReader;

import java.nio.channels.DatagramChannel;

class InternalUdpConnection extends InternalAbstractConnection {
    private final ConnectionType connectionType;
    private int prevTransmissionId;

    InternalUdpConnection(DataReader dataReader, DatagramChannel channel, Communicator communicator, ConnectionType connectionType,
                          ChannelDisconnectedHandler disconnectedHandler) {
        super(dataReader, channel, communicator, disconnectedHandler, 1);
        this.connectionType = connectionType;
    }

    @Override
    protected void onScheduleWrite(DataBuffer writeBuffer, DataBuffer data) {
        int transmissionId = incrementTransmissionId();
        writeBuffer.putBool(true);
        writeBuffer.putInt(transmissionId);
        writeBuffer.putInt(data.remaining());
        writeBuffer.put(data);
    }

    private int incrementTransmissionId() {
        return ++prevTransmissionId;
    }

    @Override
    protected boolean canWrite() {
        switch (connectionType) {
            case READ:
                return false;
            case WRITE:
            case READ_WRITE:
                return true;
            default:
                throw new IllegalStateException("Unexpected value: " + connectionType);
        }
    }

    @Override
    protected boolean canRead() {
        switch (connectionType) {
            case READ:
            case READ_WRITE:
                return true;
            case WRITE:
                return false;
            default:
                throw new IllegalStateException("Unexpected value: " + connectionType);
        }
    }
}
