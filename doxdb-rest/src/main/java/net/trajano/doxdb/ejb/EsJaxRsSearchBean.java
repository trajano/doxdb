package net.trajano.doxdb.ejb;

import static javax.json.Json.createObjectBuilder;

import java.math.BigDecimal;
import java.util.Map.Entry;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.persistence.PersistenceException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.IndexView;
import net.trajano.doxdb.SearchResult;
import net.trajano.doxdb.ext.ConfigurationProvider;
import net.trajano.doxdb.schema.IndexType;

/**
 * Handles ElasticSearch via REST API.
 *
 * @author Archimedes Trajano
 */
@Stateless
@Dependent
@LocalBean
public class EsJaxRsSearchBean implements
    DoxSearch {

    /**
     * Create a unique ID for the search index record.
     *
     * @param view
     * @return
     */
    public static String uid(final IndexView view) {

        return view.getIndex() + "\t" + view.getCollection() + "\t" + view.getDoxID();
    }

    private ConfigurationProvider configurationProvider;

    private transient EsJaxRsProvider jestProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    @Asynchronous
    public void addToIndex(final IndexView... indexViews) {

        final WebTarget target = jestProvider.getTarget().path("_bulk");

        final StringBuilder b = new StringBuilder();
        for (final IndexView indexView : indexViews) {
            b.append(
                createObjectBuilder().add("index", createObjectBuilder()
                    .add("_index", configurationProvider.getMappedIndex(indexView.getIndex()))
                    .add("_type", indexView.getCollection())
                    .add("_id", indexView.getDoxID().toString())).build().toString());
            b.append("\n");

            final JsonObjectBuilder sourceBuilder = createObjectBuilder();
            for (final Entry<String, BigDecimal> d : indexView.getNumbers()) {
                sourceBuilder.add(d.getKey(), d.getValue());
            }
            for (final Entry<String, String> d : indexView.getStrings()) {
                sourceBuilder.add(d.getKey(), d.getValue());
            }

            final JsonObjectBuilder metaBuilder = createObjectBuilder();
            for (final Entry<String, String> d : indexView.getTexts()) {
                metaBuilder.add(d.getKey(), d.getValue());
            }
            metaBuilder.add("_text", indexView.getText());
            sourceBuilder.add("_", metaBuilder);

            b.append(sourceBuilder.build().toString());
            b.append("\n");
        }
        target.request(MediaType.APPLICATION_JSON).post(Entity.entity(b.toString(), MediaType.APPLICATION_OCTET_STREAM)).readEntity(JsonObject.class);

    }

    @Override
    public SearchResult advancedSearch(final String sourceIndex,

        final JsonObject query) {

        final String index = configurationProvider.getMappedIndex(sourceIndex);
        if (index == null) {
            throw new PersistenceException("index not found");
        }

        final SearchResult result = new SearchResult();

        final JsonObject results = jestProvider.getTarget().path(index).path("_search").request(MediaType.APPLICATION_JSON).post(Entity.entity(query, MediaType.APPLICATION_JSON)).readEntity(JsonObject.class);

        final JsonArray hits = results.getJsonObject("hits").getJsonArray("hits");
        result.setTotalHits(results.getJsonObject("hits").getInt("total"));
        for (final JsonValue hitValue : hits) {
            final IndexView iv = new IndexView();
            final JsonObject hit = (JsonObject) hitValue;
            iv.setDoxID(new DoxID(hit.getString("_id")));
            iv.setCollection(hit.getString("_type"));

            for (final Entry<String, JsonValue> entry : hit.getJsonObject("_source").entrySet()) {
                if (entry.getValue() instanceof JsonNumber) {
                    iv.setNumber(entry.getKey(), ((JsonNumber) entry.getValue()).bigDecimalValue());
                } else if (entry.getValue() instanceof JsonString) {
                    iv.setString(entry.getKey(), ((JsonString) entry.getValue()).getString());
                }
            }
            result.addHit(iv);
        }

        return result;
    }

    @Override
    public SearchResult advancedSearch(final String sourceIndex,
        final String collectionName,
        final JsonObject query) {

        final String index = configurationProvider.getMappedIndex(sourceIndex);
        if (index == null) {
            throw new PersistenceException("index not found");
        }

        final SearchResult result = new SearchResult();

        final JsonObject results = jestProvider.getTarget().path(index).path(collectionName).path("_search").request(MediaType.APPLICATION_JSON).post(Entity.entity(query, MediaType.APPLICATION_JSON)).readEntity(JsonObject.class);

        final JsonArray hits = results.getJsonObject("hits").getJsonArray("hits");
        result.setTotalHits(results.getJsonObject("hits").getInt("total"));
        for (final JsonValue hitValue : hits) {
            final IndexView iv = new IndexView();
            final JsonObject hit = (JsonObject) hitValue;
            iv.setDoxID(new DoxID(hit.getString("_id")));
            iv.setCollection(hit.getString("_type"));

            for (final Entry<String, JsonValue> entry : hit.getJsonObject("_source").entrySet()) {
                if (entry.getValue() instanceof JsonNumber) {
                    iv.setNumber(entry.getKey(), ((JsonNumber) entry.getValue()).bigDecimalValue());
                } else if (entry.getValue() instanceof JsonString) {
                    iv.setString(entry.getKey(), ((JsonString) entry.getValue()).getString());
                }
            }
            result.addHit(iv);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFromIndex(final String schemaName,
        final DoxID doxID) {

        final WebTarget target = jestProvider.getTarget().path("_bulk");
        final StringBuilder b = new StringBuilder();

        for (final IndexType indexType : configurationProvider.getPersistenceConfig().getIndex()) {

            b.append(
                createObjectBuilder().add("delete", createObjectBuilder()
                    .add("_index", configurationProvider.getMappedIndex(indexType.getName()))
                    .add("_type", schemaName)
                    .add("_id", doxID.toString())).build().toString());
            b.append("\n");
        }

        target.request(MediaType.APPLICATION_JSON).post(Entity.entity(b.toString(), MediaType.APPLICATION_OCTET_STREAM)).readEntity(JsonObject.class);
    }

    /**
     * {@inheritDoc} Once the indices are removed,
     * {@link EsJaxRsProvider#init()} is called in order to recreate the
     * mappings again.
     */
    @Override
    public void reset() {

        for (final IndexType indexType : configurationProvider.getPersistenceConfig().getIndex()) {

            final WebTarget target = jestProvider.getTarget().path(configurationProvider.getMappedIndex(indexType.getName()));
            target.request(MediaType.APPLICATION_JSON).delete().readEntity(JsonObject.class);

        }
        jestProvider.init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchResult search(final String sourceIndex,
        final String queryString,
        final int limit,
        final Integer fromDoc) {

        final String index = configurationProvider.getMappedIndex(sourceIndex);
        if (index == null) {
            throw new PersistenceException("index not found");
        }

        int from = 0;
        if (fromDoc != null) {
            from = fromDoc;
        }

        final SearchResult result = new SearchResult();

        final JsonObjectBuilder sqsBuilder = Json.createObjectBuilder().add("query", queryString).add("default_operator", "and");
        final JsonObjectBuilder qBuilder = Json.createObjectBuilder().add("simple_query_string", sqsBuilder);
        final JsonObjectBuilder queryBuilder = Json.createObjectBuilder().add("size", limit).add("query", qBuilder).add("from", from);

        final JsonObject results = jestProvider.getTarget().path(index).path("_search").request(MediaType.APPLICATION_JSON).post(Entity.entity(queryBuilder.build(), MediaType.APPLICATION_JSON)).readEntity(JsonObject.class);

        final JsonArray hits = results.getJsonObject("hits").getJsonArray("hits");
        result.setTotalHits(results.getJsonObject("hits").getInt("total"));
        result.setBottomDoc(Math.min(from + hits.size(), from + limit));
        for (final JsonValue hitValue : hits) {
            final IndexView iv = new IndexView();
            final JsonObject hit = (JsonObject) hitValue;
            iv.setDoxID(new DoxID(hit.getString("_id")));
            iv.setCollection(hit.getString("_type"));

            for (final Entry<String, JsonValue> entry : hit.getJsonObject("_source").entrySet()) {
                if (entry.getValue() instanceof JsonNumber) {
                    iv.setNumber(entry.getKey(), ((JsonNumber) entry.getValue()).bigDecimalValue());
                } else if (entry.getValue() instanceof JsonString) {
                    iv.setString(entry.getKey(), ((JsonString) entry.getValue()).getString());
                }
            }
            result.addHit(iv);
        }

        return result;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchResult searchWithSchemaName(final String sourceIndex,
        final String schemaName,
        final String queryString,
        final int limit,
        final Integer fromDoc) {

        final String index = configurationProvider.getMappedIndex(sourceIndex);
        if (index == null) {
            throw new PersistenceException("index not found");
        }

        int from = 0;
        if (fromDoc != null) {
            from = fromDoc;
        }

        final SearchResult result = new SearchResult();

        final JsonObjectBuilder sqsBuilder = Json.createObjectBuilder().add("query", queryString).add("default_operator", "and");
        final JsonObjectBuilder qBuilder = Json.createObjectBuilder().add("simple_query_string", sqsBuilder);
        final JsonObjectBuilder queryBuilder = Json.createObjectBuilder()
            .add("size", limit)
            .add("query", qBuilder)
            .add("from", from);

        final JsonObject results = jestProvider.getTarget().path(index).path(schemaName).path("_search").request(MediaType.APPLICATION_JSON).post(Entity.entity(queryBuilder.build(), MediaType.APPLICATION_JSON)).readEntity(JsonObject.class);

        final JsonArray hits = results.getJsonObject("hits").getJsonArray("hits");
        result.setTotalHits(results.getJsonObject("hits").getInt("total"));
        result.setBottomDoc(Math.min(from + hits.size(), from + limit));
        for (final JsonValue hitValue : hits) {
            final IndexView iv = new IndexView();
            final JsonObject hit = (JsonObject) hitValue;
            iv.setDoxID(new DoxID(hit.getString("_id")));
            iv.setCollection(hit.getString("_type"));

            for (final Entry<String, JsonValue> entry : hit.getJsonObject("_source").entrySet()) {
                if (entry.getValue() instanceof JsonNumber) {
                    iv.setNumber(entry.getKey(), ((JsonNumber) entry.getValue()).bigDecimalValue());
                } else if (entry.getValue() instanceof JsonString) {
                    iv.setString(entry.getKey(), ((JsonString) entry.getValue()).getString());
                }
            }
            result.addHit(iv);
        }

        return result;

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

    /**
     * Sets provider.
     *
     * @param provider
     *            the provider to set
     */
    @EJB
    public void setJestProvider(final EsJaxRsProvider provider) {

        jestProvider = provider;
    }

}
