package com.gamelibrary2d.network;

import com.gamelibrary2d.io.DataBuffer;

import java.io.IOException;

public interface DataReader {

    /**
     * Reads a sequence of bytes into the given buffer.
     *
     * @param dst - The output buffer where the read data will be written
     * @return - The number of bytes read or -1 if the channel has reached end-of-stream
     */
    int read(DataBuffer dst) throws IOException;
}
