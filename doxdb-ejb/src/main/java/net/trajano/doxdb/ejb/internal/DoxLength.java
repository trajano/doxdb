package net.trajano.doxdb.ejb.internal;

public class DoxLength {

    /**
     * Access key length. 4096 bytes.
     */
    public static final int ACCESS_KEY_LENGTH = 4 * 1024;

    /**
     * Content length. 10 MB.
     */
    public static final int CONTENT_LENGTH = 10 * 1024 * 1024;

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

    /**
     * Schema name component length. 64 bytes.
     */
    public static final int SCHEMA_NAME_LENGTH = 64;
}
