package net.trajano.doxdb;

import net.trajano.doxdb.schema.DoxPersistence;

public interface ConfigurationProvider {

    DoxPersistence getPersistenceConfig();
}
