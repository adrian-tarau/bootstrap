package net.microfalx.bootstrap.model;

/**
 * A collection of constants about fields
 */
public class FieldConstants {

    /**
     * A standard property name to tag when a model was created
     */
    public static final String CREATED_AT = "createdAt";

    /**
     * A standard property name to tag when a model was modified (last time)
     */
    public static final String MODIFIED_AT = "modifiedAt";

    /**
     * A standard property name to tag when the data behind the model was received (by the current process)
     */
    public static final String RECEIVED_AT = "receivedAt";

    /**
     * A standard property name to tag when the data behind the model was sent (from a different process)
     */
    public static final String SENT_AT = "sentAt";
}
