package net.trajano.doxdb.search;

public interface DoxSearch {

    void addToIndex(IndexView indexView);

    SearchResult search(String index,
        String queryString,
        int limit);

}
