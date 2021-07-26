package com.gamelibrary2d.network.common.security;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.io.DynamicByteBuffer;
import com.gamelibrary2d.network.common.Message;
import com.gamelibrary2d.network.common.initialization.CommunicationSteps;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;

public class ServerHandshake {
    private static final SecretKeyDecrypterFactory DEFAULT_SECRET_KEY_DECRYPTER_FACTORY = DefaultRsaDecrypter::new;
    private final KeyPair keyPair;
    private final DataBuffer plaintextBuffer = new DynamicByteBuffer();
    private volatile SecretKeyDecrypterFactory secretKeyDecrypterFactory;

    public ServerHandshake(KeyPair keyPair) {
        this.keyPair = keyPair;
        secretKeyDecrypterFactory = DEFAULT_SECRET_KEY_DECRYPTER_FACTORY;
    }

    /**
     * Factory used to create a {@link Decrypter} for the secret key sent by the client.
     */
    public SecretKeyDecrypterFactory getSecretKeyDecrypterFactory() {
        return secretKeyDecrypterFactory;
    }

    /**
     * Setter for the {@link #getSecretKeyDecrypterFactory secret key decrypter factory}.
     */
    public void setSecretKeyDecrypterFactory(SecretKeyDecrypterFactory secretKeyDecrypterFactory) {
        this.secretKeyDecrypterFactory = secretKeyDecrypterFactory;
    }

    public void configure(CommunicationSteps steps) {
        sharePublicKey(steps);
        readSecretKey(steps);
    }

    private void sharePublicKey(CommunicationSteps steps) {
        steps.add((context, com) -> {
            Message message = new PublicKeyMessage(keyPair.getPublic());
            message.serializeMessage(com.getOutgoing());
        });
    }

    private void readSecretKey(CommunicationSteps steps) {
        steps.add((context, com, inbox) -> {
            int encryptionHeaderLength = inbox.getInt();
            byte[] encryptionHeader = new byte[encryptionHeaderLength];
            inbox.get(encryptionHeader);

            EncryptionReader encryptionReader = new EncryptionReader(
                    secretKeyDecrypterFactory.create(keyPair.getPrivate()));

            plaintextBuffer.clear();
            encryptionReader.readNext(inbox, plaintextBuffer);
            plaintextBuffer.flip();

            SecretKeyMessage secretKeyMessage = new SecretKeyMessage(plaintextBuffer);

            Cipher cipher = createCipher(secretKeyMessage.getCipherTransformation());

            com.setEncryptionWriter(new EncryptionWriter(
                    encryptionHeader,
                    new DefaultEncrypter(
                            secretKeyMessage.getKey(),
                            cipher,
                            secretKeyMessage.getIvLength())));

            com.setEncryptionReader(new EncryptionReader(
                    encryptionHeader,
                    new DefaultDecrypter(
                            secretKeyMessage.getKey(),
                            cipher,
                            secretKeyMessage.getIvLength())));

            return true;
        });
    }

    private Cipher createCipher(String cipherTransformation) throws IOException {
        try {
            return Cipher.getInstance(cipherTransformation);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IOException("Failed to create cipher", e);
        }
    }

    public interface SecretKeyDecrypterFactory {
        Decrypter create(PrivateKey key) throws IOException;
    }

    private static class DefaultRsaDecrypter implements Decrypter {

        /**
         * Cipher with OAEPPadding, used together with {@link #RSA_PARAMETER_SPEC}, as recommended here:
         * https://developer.android.com/guide/topics/security/cryptography#oaep-mgf1-digest
         */
        private static final String RSA_CIPHER_TRANSFORMATION = "RSA/ECB/OAEPPadding";

        /**
         * Explicit parameters to use SHA-256 for the main digest and SHA-1 for the MGF1 digest.
         */
        private static final OAEPParameterSpec RSA_PARAMETER_SPEC = new OAEPParameterSpec(
                "SHA-256", "MGF1", new MGF1ParameterSpec("SHA-1"), PSource.PSpecified.DEFAULT);

        private final PrivateKey key;
        private final Cipher cipher;

        public DefaultRsaDecrypter(PrivateKey key) {
            this.key = key;
            try {
                this.cipher = Cipher.getInstance(RSA_CIPHER_TRANSFORMATION);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                throw new RuntimeException("Failed to create cipher", e);
            }
        }

        @Override
        public byte[] decrypt(byte[] ciphertext) throws GeneralSecurityException {
            cipher.init(Cipher.DECRYPT_MODE, key, RSA_PARAMETER_SPEC);
            return cipher.doFinal(ciphertext);
        }
    }
}
