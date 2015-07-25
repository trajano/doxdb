package net.trajano.doxdb.spi;

import net.trajano.doxdb.schema.DoxPersistence;

public interface ConfigurationProvider {

    DoxPersistence getPersistenceConfig();
}
