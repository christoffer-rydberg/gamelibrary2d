package com.gamelibrary2d.network.server;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.io.DynamicByteBuffer;
import com.gamelibrary2d.io.Serializable;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.initialization.ConnectionContext;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;
import com.gamelibrary2d.network.initialization.TaskConfiguration;
import com.gamelibrary2d.network.security.*;

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

/**
 * Configures the server-side handshake which exchanges secrets with the client in order to set up encryption.
 */
public class ServerHandshakeConfiguration implements TaskConfiguration {
    private static final SecretKeyDecrypterFactory DEFAULT_SECRET_KEY_DECRYPTER_FACTORY = DefaultRsaDecrypter::new;
    private final KeyPair keyPair;
    private final DataBuffer plaintextBuffer = new DynamicByteBuffer();
    private final SecretKeyDecrypterFactory secretKeyDecrypterFactory;

    /**
     * Creates a {@link ServerHandshakeConfiguration} with default encryption settings.
     *
     * @param keyPair Holds the private and public key used to encrypt the handshake. The public key will be shared with the client.
     */
    public ServerHandshakeConfiguration(KeyPair keyPair) {
        this(keyPair, DEFAULT_SECRET_KEY_DECRYPTER_FACTORY);
    }

    /**
     * Creates a {@link ServerHandshakeConfiguration} with the specified {@link ServerHandshakeConfiguration.SecretKeyDecrypterFactory}.
     *
     * @param keyPair                   Holds the private and public key used to encrypt the handshake. The public key will be shared with the client.
     * @param secretKeyDecrypterFactory Creates a {@link Decrypter} from the secret key.
     *                                  The decrypter is used to decrypt the {@link SecretKeyMessage} that's sent from the client.
     *                                  The decrypted key is used to set up symmetric encryption between the server and the client.
     */
    public ServerHandshakeConfiguration(KeyPair keyPair, SecretKeyDecrypterFactory secretKeyDecrypterFactory) {
        this.keyPair = keyPair;
        this.secretKeyDecrypterFactory = secretKeyDecrypterFactory;
    }

    @Override
    public void addTasks(ConnectionInitializer initializer) {
        initializer.addProducer(this::sharePublicKey);
        initializer.addConsumer(this::readSecretKey);
    }

    private void sharePublicKey(ConnectionContext ctx, Communicator com) {
        Serializable message = new PublicKeyMessage(keyPair.getPublic());
        message.serialize(com.getOutgoing());
    }

    private boolean readSecretKey(ConnectionContext ctx, Communicator com, DataBuffer inbox) throws IOException {
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
