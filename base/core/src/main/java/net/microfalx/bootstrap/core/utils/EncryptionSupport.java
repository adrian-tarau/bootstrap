package net.microfalx.bootstrap.core.utils;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.lang.EncryptionUtils;
import org.springframework.stereotype.Component;

/**
 * Encryption utilities.
 */
@Component
@Getter
@Setter
public class EncryptionSupport {

    private String key = "GQsmX783Z5x2u2iA";
    private String seed = "x9qI636KL3DgrBnh";

    /**
     * Encrypts a text with a symmetric algorithm.
     *
     * @param value the value to encrypt
     * @return the encrypted value
     */
    public String encrypt(String value) {
        return EncryptionUtils.encrypt(value, key, seed);
    }

    /**
     * Decrypts a text with a symmetric algorithm.
     *
     * @param value the value to decrypt
     * @return the decrypted value
     */
    public String decrypt(String value) {
        return EncryptionUtils.decrypt(value, key, seed);
    }

    /**
     * Returns whether the text is encrypted.
     *
     * @param value the value to test
     * @return {@code true} if encrypted, {@code false} otherwise
     */
    public boolean isEncrypted(String value) {
        return EncryptionUtils.isEncrypted(value);
    }
}
