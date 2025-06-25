package io.inkHeart.cli.crypto;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

import static io.inkHeart.cli.crypto.CryptoConstants.*;


/**
 * AES-GCM is used for encryption and decryption with key derived from BouncyCastle’s Argon2BytesGenerator
 */
public class CryptoUtils {
    public static byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE];
        SecureRandom secureRandom = CryptoConstants.secureRandom;
        secureRandom.nextBytes(iv);
        return iv;
    }

    public static EncryptionResult encrypt(String plainText, SecretKey key, byte[] iv, ByteBuffer aad) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        if (aad != null) {
            cipher.updateAAD(aad);
        }
        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return new EncryptionResult(cipherText, iv, aad);
    }

    public static String decrypt(byte[] cipherText, SecretKey key, byte[] iv, ByteBuffer aad) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        if (aad != null) {
            cipher.updateAAD(aad);
        }
        byte[] decrypted = cipher.doFinal(cipherText);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public static SecretKey deriveKeyFromPassword(String password) throws Exception {
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id) // .with salt needed?
                .withParallelism(ARGON2_PARALLELISM)
                .withMemoryAsKB(ARGON2_MEMORY)
                .withIterations(ARGON2_ITERATIONS)
                .build();
        generator.init(params);
        byte[] keyBytes = new byte[KEY_SIZE / 8];
        generator.generateBytes(password.toCharArray(), keyBytes);
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * AAD = concatenate(Entry_UUID, Field_Name) separated by ':'
     * @param entryUUID Journal Entry ID
     * @param fieldName  Journal Entry field name
     * @return AAD
     */
    public static ByteBuffer populateAAD(UUID entryUUID, String fieldName) {
        ByteBuffer uuidBuffer = ByteBuffer.allocate(Long.BYTES * 2);
        uuidBuffer.putLong(entryUUID.getMostSignificantBits());
        uuidBuffer.putLong(entryUUID.getLeastSignificantBits());
        byte[] uuidBytes = uuidBuffer.array();

        ByteBuffer aadBuffer = ByteBuffer.allocate(uuidBytes.length + 1 + fieldName.length());
        aadBuffer.put(uuidBytes);
        aadBuffer.put((byte) ':');
        aadBuffer.put(fieldName.getBytes(StandardCharsets.UTF_8));

        aadBuffer.flip();
        return aadBuffer;
    }

    public static byte[] base64EncodedToBytes(String input) {
        return Base64.getDecoder().decode(input);
    }

    public static byte[] bytesToBase64Encoded(byte[] input) {
        return Base64.getEncoder().encode(input);
    }

    public static String stringToBase64Encoded(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    public record EncryptionResult(byte[] cipherText, byte[] iv, ByteBuffer aad) {
        public String getCipherTextInBase64() {
            return Base64.getEncoder().encodeToString(cipherText);
        }

        public String getIvInBase64() {
            return Base64.getEncoder().encodeToString(iv);
        }

        public ByteBuffer getAAD() {
            return aad;
        }
    }
}
