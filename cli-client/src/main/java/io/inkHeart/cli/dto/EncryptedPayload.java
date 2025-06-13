package io.inkHeart.cli.dto;

/**
 * Wraps the cipher text and it's associated IV
 * @param cipherText - In Base64 format
 * @param iv - In Base64 format
 */
public record EncryptedPayload(String cipherText, String iv) {}
