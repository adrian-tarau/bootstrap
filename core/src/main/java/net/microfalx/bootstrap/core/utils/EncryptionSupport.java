package net.microfalx.bootstrap.core.utils;

import net.microfalx.lang.ExceptionUtils;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Encryption utilities.
 */
@Component
public class EncryptionSupport {

    private String key = "9eVCsPO13tEYyiqUXdqq";
    private String seed = "rs0Ps960xKJiXrAOF6trnc34qZtgJrHW";
    private String algorithm = "AES/CBC/PKCS5PADDING";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    /**
     * Encrypts a text with a symmetric algorithm (to be recovered).
     *
     * @param value the value to encrypt
     * @return the encrypted value
     */
    public String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(seed.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            return ExceptionUtils.throwException(e);
        }
    }

    /**
     * Decrypts a text with a symmetric algorithm.
     *
     * @param encrypted the value to decrypt
     * @return the decrypted value
     */
    public String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(seed.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(original);
        } catch (javax.crypto.IllegalBlockSizeException e) {
            // we presume these exceptions means "not encrypted"
            return encrypted;
        } catch (Exception e) {
            return ExceptionUtils.throwException(e);
        }
    }
}
