package net.trajano.doxdb.internal;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.IndexView;
import net.trajano.doxdb.SearchResult;

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

    SearchResult search(String index,
        String queryString,
        int limit);

}
