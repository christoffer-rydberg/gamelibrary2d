package com.gamelibrary2d.network.common.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Decrypts ciphertexts created by the {@link DefaultEncrypter}.
 */
public class DefaultDecrypter implements Decrypter {
    private final SecretKey key;
    private final Cipher cipher;
    private final byte[] iv;

    public DefaultDecrypter(SecretKey key, Cipher cipher, int ivLength) {
        this.key = key;
        this.cipher = cipher;
        this.iv = ivLength > 0 ? new byte[ivLength] : null;
    }

    public byte[] decrypt(byte[] ciphertext) throws GeneralSecurityException {
        System.arraycopy(ciphertext, 0, iv, 0, iv.length);
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), key.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));

        byte[] cipertext = Arrays.copyOfRange(ciphertext, iv.length, ciphertext.length);

        return cipher.doFinal(cipertext);
    }
}
