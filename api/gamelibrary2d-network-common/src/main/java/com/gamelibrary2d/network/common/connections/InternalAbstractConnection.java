package com.gamelibrary2d.network.common.connections;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.io.DynamicByteBuffer;
import com.gamelibrary2d.network.common.ChannelDisconnectedHandler;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.DataReader;

import java.io.IOException;
import java.nio.channels.*;

abstract class InternalAbstractConnection implements InternalConnection {
    private final Object synchronizationKey = new Object();
    private final ByteChannel channel;
    private final DataReader dataReader;
    private final Communicator communicator;
    private final ChannelDisconnectedHandler disconnectedHandler;
    private final DataBuffer writeBuffer = new DynamicByteBuffer();
    private final int communicationChannel;
    private boolean scheduledToClose;

    public InternalAbstractConnection(
            DataReader dataReader,
            ByteChannel channel,
            Communicator communicator,
            ChannelDisconnectedHandler disconnectedHandler,
            int communicationChannel) {
        this.dataReader = dataReader;
        this.channel = channel;
        this.communicator = communicator;
        this.disconnectedHandler = disconnectedHandler;
        this.communicationChannel = communicationChannel;
    }

    @Override
    public int readIncoming() throws IOException {
        return communicator.addIncoming(communicationChannel, dataReader);
    }

    @Override
    public void addOutgoing(Selector selector, DataBuffer data) throws IOException {
        if (!canWrite()) {
            throw new IOException("Connection is read-only");
        }

        synchronized (synchronizationKey) {
            onScheduleWrite(writeBuffer, data);
            ((SelectableChannel) channel).register(selector, SelectionKey.OP_WRITE, this);
        }
    }

    @Override
    public void sendOutgoing(SelectionKey key) throws IOException {
        synchronized (synchronizationKey) {
            writeBuffer.flip();
            channel.write(writeBuffer.internalByteBuffer());
            int remaining = writeBuffer.remaining();
            if (remaining > 0) {
                int position = writeBuffer.position();
                writeBuffer.clear();
                writeBuffer.put(writeBuffer.array(), position, remaining);
                key.interestOps(SelectionKey.OP_WRITE);
            } else {
                writeBuffer.clear();
                if (canRead()) {
                    key.interestOps(SelectionKey.OP_READ);
                }
            }
        }

        if (scheduledToClose) {
            disconnect();
        }
    }

    public void closeAfterScheduledWrite() {
        synchronized (synchronizationKey) {
            if (writeBuffer.internalByteBuffer().position() > 0) {
                scheduledToClose = true;
            } else {
                disconnect();
            }
        }
    }

    @Override
    public void disconnect() {
        disconnect(null);
    }

    @Override
    public void disconnect(IOException e) {
        closeChannel();
        disconnectedHandler.onDisconnected(e);
    }

    private void closeChannel() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract boolean canWrite();

    protected abstract boolean canRead();

    protected abstract void onScheduleWrite(DataBuffer writeBuffer, DataBuffer data);
}
