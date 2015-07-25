package net.trajano.doxdb;

import net.trajano.doxdb.search.IndexView;
import net.trajano.doxdb.search.SearchResult;

public interface DoxSearch {

    void addToIndex(
        String collection,
        DoxID doxID,
        IndexView[] indexViews);

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
