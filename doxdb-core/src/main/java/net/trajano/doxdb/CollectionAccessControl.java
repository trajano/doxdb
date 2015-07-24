package net.trajano.doxdb;

import java.security.Principal;

public interface CollectionAccessControl {

    /**
     * Builds the access key based the json and principal data for creation.
     * This should throw a persistence exception if the user is not allowed to
     * create. This may return <code>null</code>
     *
     * @param collection
     *            collection
     * @param json
     *            data
     * @param principal
     *            principal
     * @return
     */
    byte[] buildAccessKeyForCreate(String collection,
        String json,
        Principal principal);

}
