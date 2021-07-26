package com.gamelibrary2d.network.common.security;

import com.gamelibrary2d.common.random.RandomInstance;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.initialization.CommunicationSteps;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;

public class ClientHandshake {
    private static final String DEFAULT_AES_CIPHER_TRANSFORMATION = "AES/CBC/PKCS5PADDING";

    private static final int AES_IV_LENGTH = 16;

    private static final SecretKeyFactory DEFAULT_SECRET_KEY_FACTORY = () ->
            new SecretKeyMessage(
                    KeyGenerator.getInstance("AES").generateKey(),
                    DEFAULT_AES_CIPHER_TRANSFORMATION,
                    AES_IV_LENGTH);

    private static final SecretKeyEncrypterFactory DEFAULT_SECRET_KEY_ENCRYPTER_FACTORY = DefaultRsaEncrypter::new;
    private volatile SecretKeyFactory secretKeyFactory;
    private volatile SecretKeyEncrypterFactory secretKeyEncrypterFactory;

    public ClientHandshake() {
        this.secretKeyFactory = DEFAULT_SECRET_KEY_FACTORY;
        this.secretKeyEncrypterFactory = DEFAULT_SECRET_KEY_ENCRYPTER_FACTORY;
    }

    /**
     * Factory used to create an {@link Encrypter} for the {@link #getSecretKeyFactory secret key} when sending it to the server.
     */
    public SecretKeyEncrypterFactory getSecretKeyEncrypterFactory() {
        return secretKeyEncrypterFactory;
    }

    /**
     * Setter for the {@link #getSecretKeyEncrypterFactory secret key encryption factory}
     */
    public void setSecretKeyEncrypterFactory(SecretKeyEncrypterFactory secretKeyEncrypterFactory) {
        this.secretKeyEncrypterFactory = secretKeyEncrypterFactory;
    }

    /**
     * Factory for the secret key that will be used to create the {@link EncryptionReader} and {@link EncryptionWriter} on the client/server {@link Communicator}.
     */
    public SecretKeyFactory getSecretKeyFactory() {
        return secretKeyFactory;
    }

    /**
     * Setter for the {@link #getSecretKeyFactory secret key factory}
     */
    public void setSecretKeyFactory(SecretKeyFactory secretKeyFactory) {
        this.secretKeyFactory = secretKeyFactory;
    }

    public void configure(CommunicationSteps steps) {
        createSecretKeyEncrypter(steps);
        shareSecretKey(steps);
    }

    private void createSecretKeyEncrypter(CommunicationSteps steps) {
        steps.add((context, com, inbox) -> {
            PublicKey publicKey = new PublicKeyMessage(inbox).getKey();
            context.register("secretKeyEncrypter", secretKeyEncrypterFactory.create(publicKey));
            return true;
        });
    }

    private byte[] createEncryptionHeader() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(RandomInstance.get().nextLong());
        return buffer.array();
    }

    private void shareSecretKey(CommunicationSteps steps) {
        steps.add((context, com) -> {
            EncryptionWriter secretKeyEncryptionWriter = new EncryptionWriter(
                    context.get(Encrypter.class, "secretKeyEncrypter"));

            SecretKeyMessage secretKeyMessage;
            try {
                secretKeyMessage = secretKeyFactory.create();
            } catch (GeneralSecurityException e) {
                throw new IOException("Failed to generate secret key", e);
            }

            byte[] encryptionHeader = createEncryptionHeader();
            com.getOutgoing().putInt(encryptionHeader.length);
            com.getOutgoing().put(encryptionHeader);

            secretKeyEncryptionWriter.write(com.getOutgoing(), secretKeyMessage::serializeMessage);

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
        });
    }

    private Cipher createCipher(String cipherTransformation) throws IOException {
        try {
            return Cipher.getInstance(cipherTransformation);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IOException("Failed to create cipher", e);
        }
    }

    public interface SecretKeyFactory {
        SecretKeyMessage create() throws GeneralSecurityException;
    }

    public interface SecretKeyEncrypterFactory {
        Encrypter create(PublicKey key) throws IOException;
    }

    private static class DefaultRsaEncrypter implements Encrypter {

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

        private final PublicKey key;
        private final Cipher cipher;

        public DefaultRsaEncrypter(PublicKey key) {
            this.key = key;
            try {
                this.cipher = Cipher.getInstance(RSA_CIPHER_TRANSFORMATION);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                throw new RuntimeException("Failed to create cipher", e);
            }
        }

        @Override
        public byte[] encrypt(byte[] plaintext) throws GeneralSecurityException {
            cipher.init(Cipher.ENCRYPT_MODE, key, RSA_PARAMETER_SPEC);
            return cipher.doFinal(plaintext);
        }
    }
}

