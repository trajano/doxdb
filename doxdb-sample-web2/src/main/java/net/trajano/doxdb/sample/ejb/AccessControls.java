package net.trajano.doxdb.sample.ejb;

import javax.ejb.Local;
import javax.ejb.Stateless;

import net.trajano.doxdb.IndexView;
import net.trajano.doxdb.ext.CollectionAccessControl;
import net.trajano.doxdb.ext.Indexer;
import net.trajano.doxdb.ext.Migrator;

@Stateless
@Local({
    CollectionAccessControl.class,
    Indexer.class,
    Migrator.class
})
public class AccessControls implements
    CollectionAccessControl,
    Migrator,
    Indexer {

    @Override
    public byte[] buildAccessKey(final String collection,
        final String json,
        final String principal) {

        return "HELLO".getBytes();
    }

    @Override
    public IndexView[] buildIndexViews(final String collection,
        final String json) {

        final IndexView indexView = new IndexView();
        indexView.appendText(json);
        indexView.setString("collection", collection);
        indexView.setIndex("myindex");

        return new IndexView[] {
            indexView
        };
    }

    @Override
    public String migrate(final String collection,
        final int oldContentVersion,
        final int newContentVersion,
        final String json) {

        return json;
    }
}
