package com.gamelibrary2d.network.connections;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.ChannelDisconnectedHandler;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.DataReader;

import java.nio.channels.SocketChannel;

class InternalTcpConnection extends InternalAbstractConnection {

    InternalTcpConnection(DataReader dataReader, SocketChannel channel, Communicator communicator, ChannelDisconnectedHandler disconnectedHandler) {
        super(dataReader, channel, communicator, disconnectedHandler, 0);
    }

    @Override
    protected void onScheduleWrite(DataBuffer writeBuffer, DataBuffer data) {
        writeBuffer.putBool(false);
        writeBuffer.putInt(data.remaining());
        writeBuffer.put(data);
    }

    @Override
    protected boolean canWrite() {
        return true;
    }

    @Override
    protected boolean canRead() {
        return true;
    }
}
