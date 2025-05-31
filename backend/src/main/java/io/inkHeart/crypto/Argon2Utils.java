package io.inkHeart.crypto;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * Argon2id is used for secure password hashing and login using argon2
 */
public class Argon2Utils {
    private static final int ITERATIONS = 3;
    private static final int MEMORY = 65536; // 64MB
    private static final int PARALLELISM = 1;

    public static String hashPassword(String password) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, password.toCharArray());
    }

    public static boolean verifyPassword(String hash, String password) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        return argon2.verify(hash, password.toCharArray());
    }
}
