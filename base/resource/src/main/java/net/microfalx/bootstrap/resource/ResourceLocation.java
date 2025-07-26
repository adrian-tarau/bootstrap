package net.microfalx.bootstrap.resource;

/**
 * An enum for the resource type.
 */
public enum ResourceLocation {

    /**
     * A resource which is persisted on local storage.
     */
    PERSISTED,

    /**
     * A resource which is persisted on local storage, but it is not guaranteed to be persisted.
     */
    TRANSIENT,

    /**
     * A resource which is persisted outside the local server (network).
     */
    SHARED;

    public static final String PERSISTED_PATH = "${user.cache}/bootstrap/persisted";
    public static final String TRANSIENT_PATH = "${user.cache}/bootstrap/transient";
    public static final String SHARED_PATH = "${user.cache}/bootstrap/shared";
}
