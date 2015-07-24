package net.trajano.doxdb;

import net.trajano.doxdb.search.IndexView;

public interface Indexer {

    /**
     * Builds the {@link IndexView} from the JSON data.
     *
     * @param collection
     * @param json
     * @return
     */
    IndexView buildIndexView(String collection,
        String json);
}
