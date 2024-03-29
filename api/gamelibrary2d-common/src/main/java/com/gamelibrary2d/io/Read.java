package com.gamelibrary2d.io;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Read {

    public static int bytes(InputStream is, DataBuffer buffer) throws IOException {
        int totalRead = 0;
        while (true) {
            int read = is.read(buffer.array(), buffer.position(), buffer.remaining());
            if (read == -1) {
                break;
            }

            totalRead += read;
            buffer.position(buffer.position() + read);

            buffer.ensureRemaining(1);
        }

        return totalRead == 0 ? -1 : totalRead;
    }

    public static int bytes(InputStream is, DataBuffer buffer, int size) throws IOException {
        if (size == 0) {
            return 0;
        }

        int totalRead = 0;
        int remaining = size;
        while (true) {
            int read = is.read(buffer.array(), buffer.position(), Math.min(buffer.remaining(), remaining));
            if (read == -1) {
                break;
            }

            totalRead += read;
            buffer.position(buffer.position() + read);

            remaining -= read;
            if (remaining == 0) {
                break;
            }

            buffer.ensureRemaining(1);
        }

        return totalRead == 0 ? -1 : totalRead;
    }

    public static byte[] byteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();

        return buffer.toByteArray();
    }

    public static DataBuffer bytes(InputStream is) throws IOException {
        DataBuffer buffer = new DynamicByteBuffer(512);
        bytes(is, buffer);
        return buffer;
    }

    public static void bytesWithSizeHeader(InputStream is, DataBuffer buffer) throws IOException {
        buffer.ensureRemaining(4);
        is.read(buffer.array(), buffer.position(), buffer.remaining());
        int length = buffer.getInt();
        buffer.position(0);
        buffer.ensureRemaining(length);
        buffer.position(is.read(buffer.array(), buffer.position(), buffer.remaining()));
    }

    public static DataBuffer bytesWithSizeHeader(InputStream is) throws IOException {
        DataBuffer buffer = new DynamicByteBuffer(Integer.BYTES);
        bytesWithSizeHeader(is, buffer);
        return buffer;
    }

    public static String text(URL url, Charset charset) throws IOException {
        try (InputStream stream = url.openStream()) {
            return text(stream, charset);
        }
    }

    public static String text(File file, Charset charset) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            return text(stream, charset);
        }
    }

    public static String text(InputStream is, Charset charset) throws IOException {
        DataBuffer buffer = new DynamicByteBuffer(512);
        bytes(is, buffer);
        buffer.flip();
        int length = buffer.remaining();
        byte[] bytes = new byte[length];
        buffer.get(bytes, 0, length);
        return new String(bytes, charset);
    }

    public static String textWithSizeHeader(DataBuffer dataBuffer, Charset charset) {
        int length = dataBuffer.getInt();
        byte[] bytes = new byte[length];
        dataBuffer.get(bytes, 0, length);
        return new String(bytes, charset);
    }

    public static String textWithSizeHeader(DataBuffer dataBuffer) {
        return textWithSizeHeader(dataBuffer, StandardCharsets.UTF_8);
    }
}