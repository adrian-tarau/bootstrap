package net.microfalx.bootstrap.web.preference;

/**
 * A class which persists encoded preferences.
 */
public interface PreferenceStorage {

    /**
     * Stores a preference related to a user.
     *
     * @param userName the user name
     * @param name     the name of the preference
     * @param value    the encoded value
     */
    void store(String userName, String name, byte[] value);

    /**
     * Loads a preference related to a user
     *
     * @param userName the user name
     * @param name     the name of the preference
     * @param name     the name
     * @return the encoded value, null if it does not exist
     */
    byte[] load(String userName, String name);
}
