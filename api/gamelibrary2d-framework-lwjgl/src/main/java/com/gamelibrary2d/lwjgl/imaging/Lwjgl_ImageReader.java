package com.gamelibrary2d.lwjgl.imaging;

import com.gamelibrary2d.imaging.DefaultImage;
import com.gamelibrary2d.imaging.Image;
import com.gamelibrary2d.imaging.ImageReader;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public final class Lwjgl_ImageReader implements ImageReader {

    @Override
    public Image read(InputStream stream) throws IOException {
        ByteBuffer imageBuffer;
        try (ReadableByteChannel rbc = Channels.newChannel(stream)) {
            imageBuffer = BufferUtils.createByteBuffer(8 * 1024);
            while (true) {
                int bytes = rbc.read(imageBuffer);
                if (bytes == -1) {
                    break;
                }
                if (imageBuffer.remaining() == 0) {
                    ByteBuffer newBuffer = BufferUtils.createByteBuffer(imageBuffer.capacity() * 3 / 2);
                    imageBuffer.flip();
                    newBuffer.put(imageBuffer);
                    imageBuffer = newBuffer;
                }
            }

            imageBuffer.flip();
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(true);

            ByteBuffer image = STBImage.stbi_load_from_memory(
                    imageBuffer, width, height, channels, 0);

            if (image == null) {
                throw new IOException("Failed to load image: " + STBImage.stbi_failure_reason());
            }

            byte[] data = new byte[image.remaining()];
            image.get(data);

            return new DefaultImage(data, width.get(0), height.get(0), channels.get(0));
        }
    }
}
