package net.microfalx.bootstrap.security;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Various utilities around security
 */
public class SecurityUtils {

    /**
     * Returns a random generated password.
     *
     * @return a non-null instance
     */
    public static String getRandomPassword() {
        return randomLong() + randomLong();
    }

    private static String randomLong() {
        return Long.toString(Math.abs(ThreadLocalRandom.current().nextLong()), Character.MAX_RADIX);
    }
}
