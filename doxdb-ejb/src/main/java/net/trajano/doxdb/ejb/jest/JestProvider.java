package net.trajano.doxdb.ejb.jest;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.PersistenceException;

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.mapping.PutMapping;
import net.trajano.doxdb.ext.ConfigurationProvider;
import net.trajano.doxdb.schema.DoxType;
import net.trajano.doxdb.schema.IndexType;

/**
 * Handles JEST searches.
 *
 * @author Archimedes
 */
@Singleton
@Startup
@LocalBean
public class JestProvider {

    private static final String MAPPING_CONFIG;

    static {
        final JsonArrayBuilder excludesBuilder = Json.createArrayBuilder().add("_.*");
        final JsonObjectBuilder sourceBuilder = Json.createObjectBuilder().add("excludes", excludesBuilder);
        final JsonObjectBuilder idBuilder = Json.createObjectBuilder().add("index", "not_analyzed").add("store", true);

        MAPPING_CONFIG = Json.createObjectBuilder().add("_source", sourceBuilder).add("_id", idBuilder).build().toString();
    }

    /**
     * Client managed by the singleton. This client already has its own
     * connection pooling so there's no need to have it managed by the
     * container.
     */
    private JestClient client;

    private ConfigurationProvider configurationProvider;

    /**
     * This will be used to drop indexes created by this provider. This is used
     * when doing testing.
     */
    @Lock(LockType.WRITE)
    public void dropIndexes() {

        for (final IndexType indexType : configurationProvider.getPersistenceConfig().getIndex()) {
            execute(new DeleteIndex.Builder(configurationProvider.getMappedIndex(indexType.getName())).build());
        }
    }

    @Lock(LockType.READ)
    public <T extends JestResult> T execute(final Action<T> clientRequest) {

        try {
            return client.execute(clientRequest);
        } catch (final IOException e) {
            throw new PersistenceException(e);
        }
    }

    @Lock(LockType.READ)
    public JestClient getClient() {

        return client;
    }

    @PostConstruct
    public void init() {

        final JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(configurationProvider.getPersistenceConfig().getElasticSearchUri())
            .multiThreaded(true)
            .build());
        client = factory.getObject();

        try {
            for (final IndexType indexType : configurationProvider.getPersistenceConfig().getIndex()) {
                final String mappedName = configurationProvider.getMappedIndex(indexType.getName());
                client.execute(new CreateIndex.Builder(mappedName).build());
                for (final DoxType doxType : configurationProvider.getPersistenceConfig().getDox()) {
                    final PutMapping putMapping = new PutMapping.Builder(
                        mappedName,
                        doxType.getName(),
                        MAPPING_CONFIG).build();
                    client.execute(putMapping);
                }
            }

        } catch (final IOException e) {
            throw new PersistenceException(e);
        }

    }

    /**
     * Sets configurationProvider.
     *
     * @param configurationProvider
     *            the configurationProvider to set
     */
    @EJB
    public void setConfigurationProvider(final ConfigurationProvider configurationProvider) {

        this.configurationProvider = configurationProvider;
    }

    @PreDestroy
    public void shutdown() {

        client.shutdownClient();
    }

}
