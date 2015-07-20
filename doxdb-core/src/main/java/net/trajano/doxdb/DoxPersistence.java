package net.trajano.doxdb;

import net.trajano.doxdb.internal.DoxXmlConfiguredDoxProvider;
import net.trajano.doxdb.spi.DoxProvider;

/**
 * Bootstrap class used to create {@link DoxProvider} instances. This is
 * implemented similar to Caching. Initial implementation will only provide the
 * DoxXmlConfiguredDoxProvider.
 *
 * @author Archimedes
 */
public final class DoxPersistence {

    private static class DoxProviderRegistry {

        private final DoxProvider doxProvider = new DoxXmlConfiguredDoxProvider();

        public DoxProvider getDoxProvider() {

            return doxProvider;
        }

    }

    private static final DoxProviderRegistry DOX_PROVIDER_REGISTRY = new DoxProviderRegistry();

    public DoxProvider getDoxProvider() {

        return DOX_PROVIDER_REGISTRY.getDoxProvider();
    }
}
