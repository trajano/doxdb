package net.trajano.doxdb;

import net.trajano.doxdb.search.IndexView;

public interface Indexer {

    /**
     * Builds the {@link IndexView} from the JSON data. Why is it an array?
     * because whoever does a search on an index will see whatever is on the
     * index. If we need to support masking of data such that one index when
     * doing a search will get masked data and another will have non-masked data
     * this is the best way around it.
     *
     * @param collection
     * @param json
     * @return an array of index views.
     */
    IndexView[] buildIndexViews(String collection,
        String json);
}
