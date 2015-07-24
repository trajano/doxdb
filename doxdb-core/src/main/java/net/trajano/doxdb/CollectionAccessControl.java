package net.trajano.doxdb;

import java.security.Principal;

public interface CollectionAccessControl {

    /**
     * Verifies if the user is allowed to create the record.
     *
     * @param storedJson
     * @param principal
     * @return
     */
    boolean allowedCreate(String storedJson,
        Principal principal);

    /**
     * Builds the access key from the json data
     *
     * @param storedJson
     * @param principal
     *            principal
     * @return
     */
    byte[] buildAccessKey(String storedJson,
        Principal principal);

}
