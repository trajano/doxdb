package net.trajano.doxdb.ejb;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import net.trajano.doxdb.ext.ConfigurationProvider;
import net.trajano.doxdb.schema.CollectionType;
import net.trajano.doxdb.schema.IndexType;

/**
 * Handles JEST searches.
 *
 * @author Archimedes
 */
@Singleton
@LocalBean
public class EsJaxRsProvider {

    private ConfigurationProvider configurationProvider;

    /**
     * @return
     */
    public WebTarget getTarget() {

        return ClientBuilder.newClient().target(configurationProvider.getPersistenceConfig().getElasticSearchUri());
    }

    /**
     * This builds the indices if needed.
     */
    @PostConstruct
    public void init() {

        for (final IndexType indexType : configurationProvider.getPersistenceConfig().getIndex()) {
            final String mappedName = configurationProvider.getMappedIndex(indexType.getName());

            if (getTarget().path(mappedName).request().head().getStatus() == 404) {
                final JsonObjectBuilder mappingBuilder = createObjectBuilder();
                for (final CollectionType collectionType : configurationProvider.getPersistenceConfig().getDox()) {
                    mappingBuilder.add(collectionType.getName(), createObjectBuilder()
                        .add("_source", createObjectBuilder()
                            .add("excludes", createArrayBuilder()
                                .add("_")
                                .add("_.*"))));
                }
                final JsonObject build = createObjectBuilder().add("mappings", mappingBuilder).build();
                getTarget().path(mappedName).request(MediaType.APPLICATION_JSON).put(Entity.entity(build, MediaType.APPLICATION_JSON)).getEntity();
            }
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

}
