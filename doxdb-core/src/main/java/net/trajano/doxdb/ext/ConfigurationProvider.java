package net.trajano.doxdb.ext;

import net.trajano.doxdb.schema.DoxPersistence;

public interface ConfigurationProvider {

    /**
     * Gets the mapped index name for a logical name.
     *
     * @param name
     * @return
     */
    String getMappedIndex(String name);

    DoxPersistence getPersistenceConfig();
}
