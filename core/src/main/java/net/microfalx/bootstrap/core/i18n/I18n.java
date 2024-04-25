package net.microfalx.bootstrap.core.i18n;

/**
 * An interface to resolve internationalized texts.
 */
public interface I18n {

    /**
     * Returns the internationalized enum value.
     *
     * @param value the enum
     * @return the value, null if not defined
     */
    <E extends Enum<E>> String getText(E value);

    /**
     * Returns the value associated with a key
     *
     * @param key the key
     * @return the value, null if not defined
     */
    String getText(String key);

    /**
     * Returns the value associated with a key
     *
     * @param key      the key
     * @param safeText {@code true} to return a safe text when the I18n is missing, {@code false} otherwise
     * @return the value, null if not defined
     */
    String getText(String key, boolean safeText);

    /**
     * Returns the value associated with a key and format the message using given arguments
     *
     * @param key       the key
     * @param arguments the arguments passed to the message formatter
     * @return the value, null if not defined
     */
    String getText(String key, Object... arguments);

    /**
     * Returns the value associated with a key and format the message using given arguments
     *
     * @param key       the key
     * @param safeText  {@code true} to return a safe text when the I18n is missing, {@code false} otherwise
     * @param arguments the arguments passed to the message formatter
     * @return the value, null if not defined
     */
    String getText(String key, boolean safeText, Object... arguments);
}
