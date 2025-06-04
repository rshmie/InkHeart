package io.inkHeart.crypto;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import static io.inkHeart.crypto.CryptoConstants.*;

/**
 * Argon2id is used for secure password hashing and login using argon2
 */
public class Argon2Utils {

    public static String hashPassword(String password) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        return argon2.hash(ARGON2_ITERATIONS, ARGON2_MEMORY, ARGON2_PARALLELISM, password.toCharArray());
    }

    public static boolean verifyPassword(String hash, String password) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        return argon2.verify(hash, password.toCharArray());
    }
}
