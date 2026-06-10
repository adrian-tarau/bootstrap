package net.microfalx.bootstrap.test;

import net.microfalx.lang.ClassUtils;

/**
 * Various utilities around tests.
 */
public class TestUtils {

    private static volatile Boolean HAS_JPA;

    /**
     * Returns whether the JPA libraries are available.
     *
     * @return {@code true} if available, {@code false} otherwise
     */
    public static boolean isJpaAvailable() {
        if (HAS_JPA == null) {
            HAS_JPA = ClassUtils.exists("org.springframework.data.jpa.repository.JpaRepository");
        }
        return HAS_JPA;
    }

}
