package io.inkHeart.cli.crypto;

import org.junit.jupiter.api.Test;
import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilsTest {

    @Test
    public void testGenerateIV() {
        byte[] iv1 = CryptoUtils.generateIV();
        byte[] iv2 = CryptoUtils.generateIV();
        assertNotNull(iv1);
        assertNotNull(iv2);
        assertEquals(CryptoConstants.IV_SIZE, iv1.length);
        assertFalse(Arrays.equals(iv1, iv2));
    }

    @Test
    public void testEncryptionAndDecryption() throws Exception {
        String password = "strongPassword123!";
        String plaintext = "Hello, secure world!";
        byte[] iv = CryptoUtils.generateIV();
        SecretKey key = CryptoUtils.deriveKeyFromPassword(password);
        byte[] aad = "associatedData".getBytes();

        CryptoUtils.EncryptionResult result = CryptoUtils.encrypt(plaintext, key, iv, ByteBuffer.wrap(aad));
        assertNotNull(result);
        assertNotNull(result.cipherText());
        assertNotNull(result.getCipherTextInBase64());
        assertNotNull(result.getIvInBase64());

        String decrypted = CryptoUtils.decrypt(result.cipherText(), key, iv, ByteBuffer.wrap(aad));
        assertEquals(plaintext, decrypted);
    }

    @Test
    public void testDecryptWithIncorrectAADFails() throws Exception {
        String plaintext = "Sensitive text";
        String password = "anotherPassword";
        byte[] iv = CryptoUtils.generateIV();
        SecretKey key = CryptoUtils.deriveKeyFromPassword(password);
        byte[] correctAAD = "correctAAD".getBytes();
        byte[] wrongAAD = "wrongAAD".getBytes();

        CryptoUtils.EncryptionResult result = CryptoUtils.encrypt(plaintext, key, iv, ByteBuffer.wrap(correctAAD));
        assertThrows(Exception.class, () -> {
            CryptoUtils.decrypt(result.cipherText(), key, iv, ByteBuffer.wrap(wrongAAD));
        });
    }

    @Test
    public void testBase64EncodingAndDecoding() {
        String input = "Test string for base64";
        String encoded = CryptoUtils.stringToBase64Encoded(input);
        byte[] decodedBytes = CryptoUtils.base64EncodedToBytes(encoded);
        assertEquals(input, new String(decodedBytes));
    }

    @Test
    public void testBytesToBase64Encoded() {
        byte[] inputBytes = "hello".getBytes();
        byte[] encodedBytes = CryptoUtils.bytesToBase64Encoded(inputBytes);
        assertEquals("aGVsbG8=", new String(encodedBytes));
    }

    @Test
    public void testDeriveKeyFromPasswordConsistency() throws Exception {
        String password = "constantPassword";
        SecretKey key1 = CryptoUtils.deriveKeyFromPassword(password);
        SecretKey key2 = CryptoUtils.deriveKeyFromPassword(password);
        assertArrayEquals(key1.getEncoded(), key2.getEncoded());
    }
    @Test
    public void testPopulateAAD() {
        UUID uuid = UUID.randomUUID();
        String fieldName = "title";
        ByteBuffer actual = CryptoUtils.populateAAD(uuid, fieldName);
        byte[] fieldBytes = fieldName.getBytes(StandardCharsets.UTF_8);

        ByteBuffer expected = ByteBuffer.allocate(16 + 1 + fieldBytes.length);
        expected.put(uuidToBytes(uuid));
        expected.put((byte) ':');
        expected.put(fieldBytes);
        expected.flip();

        assertEquals(expected, actual);
    }

    private static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

}