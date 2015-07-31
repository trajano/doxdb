package net.trajano.doxdb.ejb.jest;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.PersistenceException;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.indices.CreateIndex;
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

    private JestClient client;

    @EJB
    private ConfigurationProvider configurationProvider;

    @PostConstruct
    public void createIndices() {

        final JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder("http://localhost:9200")
            .multiThreaded(true)
            .build());
        client = factory.getObject();

        try {
            for (final IndexType indexType : configurationProvider.getPersistenceConfig().getIndex()) {
                client.execute(new CreateIndex.Builder(indexType.getName()).build());
                for (final DoxType doxType : configurationProvider.getPersistenceConfig().getDox()) {
                    final PutMapping putMapping = new PutMapping.Builder(
                        indexType.getName(),
                        doxType.getName(),
                        MAPPING_CONFIG).build();
                    client.execute(putMapping);
                }
            }

        } catch (final IOException e) {
            throw new PersistenceException();
        }

    }

    public JestClient getClient() {

        return client;
    }

    @PreDestroy
    public void shutdown() {

        client.shutdownClient();
    }

}
