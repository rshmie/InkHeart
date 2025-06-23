package io.inkHeart.cli.crypto;

import java.security.SecureRandom;

public final class CryptoConstants {
    public static final int ARGON2_ITERATIONS = 3;
    public static final int ARGON2_MEMORY = 65536; // 64MB
    public static final int ARGON2_PARALLELISM = 1;
    public static final SecureRandom secureRandom = new SecureRandom();
    public static final int KEY_SIZE = 256;
    public static final int IV_SIZE = 12; // 96 bits for GCM
    public static final int GCM_TAG_LENGTH = 128;

}
