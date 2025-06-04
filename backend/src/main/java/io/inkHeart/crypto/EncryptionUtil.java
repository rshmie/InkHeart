package io.inkHeart.crypto;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import static io.inkHeart.crypto.CryptoConstants.*;

/**
 * AES-GCM is used for encryption and decryption with key derived from BouncyCastle’s Argon2BytesGenerator
 */
public class EncryptionUtil {
    public static byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return iv;
    }

    public static EncryptionResult encrypt(String plainText, SecretKey key, byte[] iv, byte[] aad) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        if (aad != null) {
            cipher.updateAAD(aad);
        }
        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return new EncryptionResult(cipherText, iv, aad);
    }

    public static String decrypt(byte[] cipherText, SecretKey key, byte[] iv, byte[] aad) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        if (aad != null) {
            cipher.updateAAD(aad);
        }
        byte[] decrypted = cipher.doFinal(cipherText);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public static SecretKey deriveKeyFromPassword(String password, byte[] salt) throws Exception {
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withParallelism(ARGON2_PARALLELISM)
                .withMemoryAsKB(ARGON2_MEMORY)
                .withIterations(ARGON2_ITERATIONS)
                .build();
        generator.init(params);
        byte[] keyBytes = new byte[KEY_SIZE / 8];
        generator.generateBytes(password.toCharArray(), keyBytes);
        return new SecretKeySpec(keyBytes, "AES");
    }

    public record EncryptionResult(byte[] cipherText, byte[] iv, byte[] aad) {
        public String getCipherTextInBase64() {
            return Base64.getEncoder().encodeToString(cipherText);
        }

        public String getIvInBase64() {
            return Base64.getEncoder().encodeToString(iv);
        }
    }
}
