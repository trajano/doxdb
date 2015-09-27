package net.trajano.doxdb.ejb.internal;

/**
 * Defines the field lengths for DoxDB data structures.
 *
 * @author Archimedes Trajano
 */
public class DoxLength {

    /**
     * Access key length. 4096 bytes.
     */
    public static final int ACCESS_KEY_LENGTH = 4 * 1024;

    /**
     * Collection name length. 64 bytes.
     */
    public static final int COLLECTION_NAME_LENGTH = 32;

    /**
     * Content length. 10 MB.
     */
    public static final int CONTENT_LENGTH = 10 * 1024 * 1024;

    /**
     * Index file length. 1GB.
     */
    public static final int INDEX_FILE_LENGTH = 1024 * 1024 * 1024;

    /**
     * Index file name length. 64 bytes.
     */
    public static final int INDEX_FILE_NAME_LENGTH = 64;

    /**
     * Index name length. 64 bytes.
     */
    public static final int INDEX_NAME_LENGTH = 64;

    /**
     * Lookup key length.
     */
    public static final int LOOKUP_KEY_LENGTH = 128;

    public static final int LOOKUP_NAME_LENGTH = 32;

    /**
     * OOB length. 20 MB.
     */
    public static final int OOB_LENGTH = 20 * 1024 * 1024;

    /**
     * OOB name length. 64 bytes.
     */
    public static final int OOB_NAME_LENGTH = 64;

    /**
     * Principal name length. 128 bytes.
     */
    public static final int PRINCIPAL_LENGTH = 128;
}
