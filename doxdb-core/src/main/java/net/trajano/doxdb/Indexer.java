package net.trajano.doxdb;

import net.trajano.doxdb.search.IndexView;

public interface Indexer {

    IndexView buildIndexView(String json);
}
