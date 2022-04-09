package com.gamelibrary2d.animations.formats.gif;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import static com.gamelibrary2d.animations.formats.gif.InternalInputStreamExtensions.readOrThrow;

class InternalGifFrame {
    private static final int IMAGE_SEPARATOR = 0x2C;
    private static final int EXTENSION_CODE = 0x21;
    private static final int GRAPHIC_CONTROL_EXTENSION = 0xf9;
    private static final int TERMINATOR = 0x3b;

    final InternalImageData imageData;
    final InternalImageDescriptor imageDescriptor;
    final InternalColorTable localColorTable;
    final InternalGraphicControlExtension graphicControlExtension;

    InternalGifFrame(
            InternalImageData imageData,
            InternalImageDescriptor imageDescriptor,
            InternalColorTable localColorTable,
            InternalGraphicControlExtension graphicControlExtension) {
        this.imageData = imageData;
        this.imageDescriptor = imageDescriptor;
        this.localColorTable = localColorTable;
        this.graphicControlExtension = graphicControlExtension;
    }

    private static int skipSubBlock(final InputStream is) throws IOException {
        int blockSize = 0xff & readOrThrow(is);

        long remaining = blockSize;
        while (remaining > 0) {
            long result = is.skip(remaining);
            if(result <= 0) {
                throw new IOException("Failed to skip block of size " + blockSize + ". Bytes read: " + (blockSize - remaining));
            }
            remaining -= result;
        }

        return blockSize;
    }

    private static void skipSubBlocks(InputStream is) throws IOException {
        while (skipSubBlock(is) > 0) { }
    }

    public static InternalGifFrame read(InputStream is, ByteOrder byteOrder) throws IOException {
        InternalGraphicControlExtension graphicControlExtension = null;

        while (true) {
            int blockCode = is.read();

            switch (blockCode) {
                case IMAGE_SEPARATOR:
                    InternalImageDescriptor imageDescriptor = InternalImageDescriptor.read(is, byteOrder);
                    InternalColorTable localColorTable = imageDescriptor.localColorTableFlag
                            ? InternalColorTable.read(is, imageDescriptor.sizeOfLocalColorTable)
                            : null;
                    InternalImageData imageData = InternalImageData.read(is, imageDescriptor.imageWidth * imageDescriptor.imageHeight, byteOrder);
                    return new InternalGifFrame(imageData, imageDescriptor, localColorTable, graphicControlExtension);

                case EXTENSION_CODE:
                    int extensionCode = is.read();
                    if (extensionCode == GRAPHIC_CONTROL_EXTENSION) {
                        if (graphicControlExtension != null) {
                            throw new IOException("Invalid GIF. Frame contains more than one graphic control extension.");
                        }

                        graphicControlExtension = InternalGraphicControlExtension.read(is, byteOrder);
                    } else {
                        // We are not interested in other extensions. Skip ahead.
                        skipSubBlocks(is);
                    }
                    break;

                case 0x00: // Trailing 0-bytes are occurring in some GIFS. Ignore and keep going.
                    break;

                case TERMINATOR:
                case -1:
                    return null;

                default:
                    throw new IOException("Invalid block code: " + blockCode);
            }
        }
    }
}
