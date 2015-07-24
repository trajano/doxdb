package net.trajano.doxdb;

import javax.ejb.Asynchronous;

import net.trajano.doxdb.search.IndexView;
import net.trajano.doxdb.search.SearchResult;

public interface DoxSearch {

    @Asynchronous
    void addToIndex(String index,
        String collection,
        DoxID doxID,
        IndexView indexView);

    @Asynchronous
    void removeFromIndex(String index,
        DoxID doxID);

    SearchResult search(String index,
        String queryString,
        int limit);

}
