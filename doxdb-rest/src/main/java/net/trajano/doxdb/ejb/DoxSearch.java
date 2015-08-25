package net.trajano.doxdb.ejb;

import javax.ejb.Local;

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

}
