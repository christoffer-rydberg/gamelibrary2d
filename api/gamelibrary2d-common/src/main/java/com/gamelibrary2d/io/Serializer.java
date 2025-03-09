package com.gamelibrary2d.io;

import com.gamelibrary2d.functional.Func;

import java.io.*;
import java.net.URL;

public class Serializer {
    private final DataBuffer buffer;

    public Serializer() {
        this.buffer = new DynamicByteBuffer();
    }

    public Serializer(DataBuffer buffer) {
        this.buffer = buffer;
    }

    public void serialize(Serializable target, OutputStream stream) throws IOException {
        buffer.clear();
        target.serialize(buffer);
        buffer.flip();
        Write.bytes(buffer, stream);
    }

    public void serialize(Serializable target, File file, boolean overwrite) throws IOException {
        buffer.clear();
        target.serialize(buffer);
        buffer.flip();
        Write.bytes(buffer, file, overwrite);
    }

    public <T> T deserialize(InputStream stream, Func<DataBuffer, T> factory) throws IOException {
        buffer.clear();
        Read.bytes(stream, buffer);
        buffer.flip();
        return factory.invoke(buffer);
    }

    public <T> T deserialize(File file, Func<DataBuffer, T> factory) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            return deserialize(stream, factory);
        }
    }

    public <T> T deserialize(URL url, Func<DataBuffer, T> factory) throws IOException {
        try (InputStream stream = url.openStream()) {
            return deserialize(stream, factory);
        }
    }
}
