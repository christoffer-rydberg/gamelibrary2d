package com.gamelibrary2d.network.common.connections;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.network.common.DataReader;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

class InternalByteChannelReader implements DataReader {
    private final ReadableByteChannel channel;

    public InternalByteChannelReader(ReadableByteChannel channel) {
        this.channel = channel;
    }

    @Override
    public int read(DataBuffer output) throws IOException {
        int totalBytesRead = 0;

        while (true) {
            output.ensureRemaining(1);
            int remaining = output.remaining();

            int bytesRead = channel.read(output.internalByteBuffer());
            if (bytesRead == -1 && totalBytesRead == 0) {
                return -1;
            }

            totalBytesRead += bytesRead;
            if (bytesRead < remaining) {
                break;
            }
        }

        return totalBytesRead;
    }
}
