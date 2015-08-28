package net.trajano.doxdb.ext;

import net.trajano.doxdb.schema.DoxPersistence;

public interface ConfigurationProvider {

    /**
     * Gets the mapped index name for a logical name.
     *
     * @param name
     *            logical name
     * @return physical name
     */
    String getMappedIndex(String name);

    DoxPersistence getPersistenceConfig();
}
