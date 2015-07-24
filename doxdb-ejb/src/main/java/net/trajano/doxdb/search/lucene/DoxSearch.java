package net.trajano.doxdb.search.lucene;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.search.IndexView;
import net.trajano.doxdb.search.SearchResult;

public interface DoxSearch {

    void addToIndex(String index,
        DoxID doxID,
        IndexView indexView);

    void removeFromIndex(String index,
        DoxID doxID);

    SearchResult search(String index,
        String queryString,
        int limit);

}
