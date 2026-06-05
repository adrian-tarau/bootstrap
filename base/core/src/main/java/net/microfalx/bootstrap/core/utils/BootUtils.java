package net.microfalx.bootstrap.core.utils;

import net.microfalx.lang.StringUtils;

/**
 * Various utilities for core bootstrap generic features.
 */
public class BootUtils {

    public static final String CLI_SYS_PROP = "bootstrap.cli";

    /**
     * Enables (using a system property) that the application is a CLI application.
     * <p>
     * The option can be used by various services
     */
    public static void asCli() {
        System.setProperty(CLI_SYS_PROP, "true");
    }

    /**
     * Returns whether the application is a CLI application.
     *
     * @return {@code true} if CLI, {@code false} otherwise
     */
    public static boolean isCli() {
        return StringUtils.asBoolean(System.getProperty(CLI_SYS_PROP), false);
    }
}
