package com.gamelibrary2d.network.security;

import com.gamelibrary2d.functional.ParameterizedAction;
import com.gamelibrary2d.io.DataBuffer;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class EncryptionWriter {
    private final Encrypter encrypter;
    private final byte[] encryptionHeader;

    /**
     * @param encrypter Used to encrypt data.
     */
    public EncryptionWriter(Encrypter encrypter) {
        this.encrypter = encrypter;
        this.encryptionHeader = null;
    }

    /**
     * @param encryptionHeader Header sent before each encrypted message.
     * @param encrypter        Used to encrypt data.
     */
    public EncryptionWriter(byte[] encryptionHeader, Encrypter encrypter) {
        this.encryptionHeader = encryptionHeader;
        this.encrypter = encrypter;
    }

    /**
     * Writes encrypted data to the specified buffer.
     *
     * @param plaintextWriter Writes plain data to the buffer.
     */
    public void write(DataBuffer buffer, ParameterizedAction<DataBuffer> plaintextWriter) throws IOException {
        int position = buffer.position();

        // Write data and restore position
        plaintextWriter.perform(buffer);
        int positionAfter = buffer.position();
        buffer.position(position);

        // Read data and restore position
        byte[] bytes = new byte[positionAfter - position];
        buffer.get(bytes);
        buffer.position(position);

        // Encrypt
        byte[] encrypted;
        try {
            encrypted = encrypter.encrypt(bytes);
        } catch (GeneralSecurityException e) {
            throw new IOException("Encryption failed", e);
        }

        // Write encrypted
        if (encryptionHeader != null) {
            buffer.put(encryptionHeader);
        }
        buffer.putInt(encrypted.length);
        buffer.put(encrypted);
    }
}
