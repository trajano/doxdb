package net.trajano.doxdb.ejb.jest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.PersistenceException;

import io.searchbox.client.JestResult;
import io.searchbox.core.Bulk;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.indices.DeleteIndex;
import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.IndexView;
import net.trajano.doxdb.SearchResult;
import net.trajano.doxdb.ext.ConfigurationProvider;
import net.trajano.doxdb.ext.DoxSearch;
import net.trajano.doxdb.schema.IndexType;

/**
 * Handles JEST searches.
 *
 * @author Archimedes
 */
@Stateless
@Remote(DoxSearch.class)
public class JestDoxSearchBean implements
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

    private transient JestProvider jestProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    @Asynchronous
    public void addToIndex(final IndexView... indexViews) {

        final Bulk.Builder bulkBuilder = new Bulk.Builder();
        for (final IndexView indexView : indexViews) {

            final Map<String, Object> map = new HashMap<>();
            for (final Entry<String, Double> d : indexView.getDoubles()) {
                map.put(d.getKey(), d.getValue());
            }
            for (final Entry<String, Long> d : indexView.getLongs()) {
                map.put(d.getKey(), d.getValue());
            }
            for (final Entry<String, String> d : indexView.getStrings()) {
                map.put(d.getKey(), d.getValue());
            }
            final Map<String, Object> metaMap = new HashMap<>();
            for (final Entry<String, String> d : indexView.getTexts()) {
                metaMap.put(d.getKey(), d.getValue());
            }
            metaMap.put("_text", indexView.getText());

            final Index index = new Index.Builder(map).index(configurationProvider.getMappedIndex(indexView.getIndex())).type(indexView.getCollection()).id(indexView.getDoxID().toString()).build();

            bulkBuilder.addAction(index);
        }
        final JestResult result = jestProvider.execute(bulkBuilder.build());
        if (!result.isSucceeded()) {
            throw new PersistenceException(result.getJsonString());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFromIndex(final String schemaName,
        final DoxID doxID) {

        final Bulk.Builder bulkBuilder = new Bulk.Builder();
        for (final IndexType indexType : configurationProvider.getPersistenceConfig().getIndex()) {

            final Delete del = new Delete.Builder(doxID.toString()).type(schemaName).index(configurationProvider.getMappedIndex(indexType.getName())).build();

            bulkBuilder.addAction(del);
        }
        jestProvider.execute(bulkBuilder.build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {

        jestProvider.execute(new DeleteIndex.Builder("_all").build());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({
        "rawtypes",
        "deprecation"
    })
    public SearchResult search(final String index,
        final String queryString,
        final int limit,
        final Integer fromDoc) {

        int from = 0;
        if (fromDoc != null) {
            from = fromDoc;
        }

        final SearchResult result = new SearchResult();

        final JsonObjectBuilder sqsBuilder = Json.createObjectBuilder().add("query", queryString).add("default_operator", "and");
        final JsonObjectBuilder qBuilder = Json.createObjectBuilder().add("simple_query_string", sqsBuilder);
        final JsonObjectBuilder queryBuilder = Json.createObjectBuilder().add("size", limit).add("query", qBuilder).add("from", from);
        final String query = queryBuilder.build().toString();

        final SearchResult2 esResults = new SearchResult2(jestProvider.execute(new Search.Builder(query).addIndex(index).build()));
        result.setTotalHits(esResults.getTotal());

        final List<Map> hits = esResults.getSourceAsObjectList(Map.class);
        result.setBottomDoc(Math.min(from + hits.size(), from + limit));
        for (final Map hit : hits) {
            final IndexView iv = new IndexView();
            for (final Object key : hit.keySet()) {
                final Object value = hit.get(key);
                if (JestResult.ES_METADATA_ID.equals(key)) {
                    iv.setDoxID(new DoxID((String) value));
                } else if (SearchResult2.ES_METADATA_TYPE.equals(key)) {
                    iv.setCollection((String) value);
                } else if (value instanceof Double) {
                    iv.setDouble(key.toString(), (double) value);
                } else if (value instanceof Long) {
                    iv.setLong(key.toString(), (long) value);
                } else if (value instanceof String) {
                    iv.setString((String) key, (String) value);
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
    public void setJestProvider(final JestProvider provider) {

        jestProvider = provider;
    }

}
