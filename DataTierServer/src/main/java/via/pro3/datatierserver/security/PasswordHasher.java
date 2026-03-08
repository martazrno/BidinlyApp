package via.pro3.datatierserver.security;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class PasswordHasher {

    private static final Argon2 argon2 = Argon2Factory.create(
            Argon2Factory.Argon2Types.ARGON2id
    );

    private static final int ITERATIONS = 3;
    private static final int MEMORY = 65536;   // 64MB
    private static final int PARALLELISM = 2;

    public static String hash(String password) {
        return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, password);
    }

    public static boolean verify(String hash, String password) {
        return argon2.verify(hash, password);
    }
}