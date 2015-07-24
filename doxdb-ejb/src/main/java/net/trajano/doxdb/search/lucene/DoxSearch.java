package net.trajano.doxdb.search.lucene;

import net.trajano.doxdb.search.IndexView;
import net.trajano.doxdb.search.SearchResult;

public interface DoxSearch {

    void addToIndex(IndexView indexView);

    SearchResult search(String index,
        String queryString,
        int limit);

}
