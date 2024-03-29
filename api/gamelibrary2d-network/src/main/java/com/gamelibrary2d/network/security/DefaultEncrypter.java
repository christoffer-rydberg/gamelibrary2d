package com.gamelibrary2d.network.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Encrypts data in order to provide confidentiality. Note that integrity
 * is not guaranteed as neither the IV nor the cipher text is authenticated,
 * e.g. using HMAC. The reason is to favor performance over integrity, especially
 * since all other messages are sent in plaintext without authentication.
 * The {@link DefaultDecrypter} is used for decryption.
 */
public class DefaultEncrypter implements Encrypter {
    private final SecretKey key;
    private final Cipher cipher;
    private final byte[] iv;
    private final Random secureRandom = new SecureRandom();

    public DefaultEncrypter(SecretKey key, Cipher cipher, int ivLength) {
        this.key = key;
        this.cipher = cipher;
        this.iv = new byte[ivLength];
    }

    private static byte[] combine(byte[] iv, byte[] ciphertext) {
        int length = iv.length + ciphertext.length;
        byte[] result = new byte[length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);
        return result;
    }

    @Override
    public byte[] encrypt(byte[] plaintext) throws GeneralSecurityException {
        secureRandom.nextBytes(iv);

        cipher.init(
                Cipher.ENCRYPT_MODE,
                new SecretKeySpec(key.getEncoded(), key.getAlgorithm()),
                new IvParameterSpec(iv));

        return combine(cipher.getIV(), cipher.doFinal(plaintext));
    }
}
