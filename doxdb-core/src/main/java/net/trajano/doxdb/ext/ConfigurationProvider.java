package net.trajano.doxdb.ext;

import net.trajano.doxdb.schema.DoxPersistence;

public interface ConfigurationProvider {

    DoxPersistence getPersistenceConfig();
}
