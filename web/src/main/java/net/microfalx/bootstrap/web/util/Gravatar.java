package net.microfalx.bootstrap.web.util;

import net.microfalx.lang.Hashing;
import net.microfalx.lang.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Creates the URL to access the avatar associated with the email address.
 */
public class Gravatar {

    private static final String ANONYMOUS_EMAIL = "anonymous@users.org";

    private final String email;

    public Gravatar(String email) {
        this.email = StringUtils.defaultIfEmpty(email, ANONYMOUS_EMAIL);
    }

    /**
     * Returns the URL to get the image.
     *
     * @return a non-null instance
     */
    public String getUrl() {
        String emailLc = this.email.toLowerCase();
        String hash = sha256Hex(emailLc);
        return "https://gravatar.com/avatar/" + hash;
    }

    private static String hex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i]
                    & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    private String sha256Hex(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return hex(md.digest(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            // ignore, should not happen
        }
        return Hashing.hash(message);
    }
}
