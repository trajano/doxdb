package net.trajano.doxdb;

import net.trajano.doxdb.search.IndexView;
import net.trajano.doxdb.search.SearchResult;

public interface DoxSearch {

    void addToIndex(String index,
        String collection,
        DoxID doxID,
        IndexView indexView);

    void removeFromIndex(String index,
        DoxID doxID);

    SearchResult search(String index,
        String queryString,
        int limit);

}
