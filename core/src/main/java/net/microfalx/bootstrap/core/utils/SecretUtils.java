package net.microfalx.bootstrap.core.utils;

import java.util.HashSet;
import java.util.Set;

import static net.microfalx.lang.StringUtils.isEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * Utilities around secrets (password, tokens, etc)
 */
public class SecretUtils {

    private static final Set<String> maskSecret = new HashSet<>();
    private static final String MASK_FRAGMENT = "*****************";
    private static final String MASK = "*******************";

    /**
     * Encrypts a text with a symmetric algorithm.
     *
     * @param value the value to encrypt
     * @return the encrypted value
     */
    public String encrypt(String value) {
        return new EncryptionSupport().encrypt(value);
    }

    /**
     * Decrypts a text with a symmetric algorithm.
     *
     * @param value the value to decrypt
     * @return the decrypted value
     */
    public String decrypt(String value) {
        return new EncryptionSupport().decrypt(value);
    }

    /**
     * Return whether the key (property) contains a keyword which suggests the value associated with the key
     * is a secret and should not be displayed as is.
     *
     * @param key the key/property
     * @return <code>true</code> if it contains a secret, <code>false</code> otherwise
     */
    public static boolean isSecret(String key) {
        if (isEmpty(key)) return false;
        for (String secret : maskSecret) {
            if (toIdentifier(key).contains(secret)) return true;
        }
        return false;
    }

    /**
     * Masks a secret to see only 1/10 of the secret from beginning and end.
     *
     * @param secret the secret
     * @return the secret masked
     */
    public static String maskSecret(String secret) {
        if (isEmpty(secret)) return MASK;
        int length = secret.length();
        if (length <= 5) {
            return secret.charAt(0) + MASK_FRAGMENT;
        } else {
            int charCount = Math.max(1, length / 10);
            return secret.substring(0, charCount) + MASK_FRAGMENT + secret.substring(length - charCount, length);
        }
    }


    static {
        maskSecret.add("password");
        maskSecret.add("secret");
        maskSecret.add("apikey");
        maskSecret.add("api_key");
        maskSecret.add("bearer");
    }
}
