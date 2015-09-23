package net.trajano.doxdb.ejb;

import javax.ejb.Local;
import javax.json.JsonObject;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.IndexView;
import net.trajano.doxdb.SearchResult;

@Local
public interface DoxSearch {

    /**
     * This must only be one thread at a time.
     *
     * @param indexViews
     */
    void addToIndex(IndexView... indexViews);

    /**
     * Performs an advanced query that allows for near arbitrary searches on a
     * given collection.
     *
     * @param sourceIndex
     * @param query
     * @return
     */
    SearchResult advancedSearch(String sourceIndex,
        String schemaName,
        JsonObject query);

    /**
     * This must only be one thread at a time.
     */
    void removeFromIndex(String collection,
        DoxID doxID);

    /**
     * This will clear all the indexing data from the system.
     */
    void reset();

    /**
     * @param index
     * @param queryString
     * @param limit
     *            maximum number of results.
     * @param fromDoc
     *            return the results starting from this identifier. This may be
     *            <code>null</code>.
     * @return
     */
    SearchResult search(String index,
        String queryString,
        int limit,
        Integer fromDoc);

    SearchResult searchWithSchemaName(String index,
        String schemaName,
        String queryString,
        int limit,
        Integer fromDoc);

}
