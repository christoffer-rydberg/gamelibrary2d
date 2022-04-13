package com.gamelibrary2d.common.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class ResourceWriter {
    private final DataBuffer buffer;

    public ResourceWriter() {
        this.buffer = new DynamicByteBuffer();
    }

    public ResourceWriter(DataBuffer buffer) {
        this.buffer = buffer;
    }

    public void write(Serializable target, OutputStream stream) throws IOException {
        buffer.clear();
        target.serialize(buffer);
        buffer.flip();
        Write.bytes(buffer, stream);
    }

    public void write(Serializable target, File file, boolean overwrite) throws IOException {
        buffer.clear();
        target.serialize(buffer);
        buffer.flip();
        Write.bytes(buffer, file, overwrite);
    }
}
