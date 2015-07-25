package net.trajano.doxdb.sample.test;

import java.security.Principal;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import net.trajano.doxdb.CollectionAccessControl;
import net.trajano.doxdb.Indexer;
import net.trajano.doxdb.Migrator;
import net.trajano.doxdb.search.IndexView;

@Stateless
@Remote({
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
        final Principal principal) {

        return "HELLO".getBytes();
    }

    @Override
    public IndexView[] buildIndexViews(final String collection,
        final String json) {

        final IndexView indexView = new IndexView();
        indexView.setText("json", json);
        indexView.appendText(json);
        indexView.setString("collection", collection);
        indexView.setIndex("MYINDEX");
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
