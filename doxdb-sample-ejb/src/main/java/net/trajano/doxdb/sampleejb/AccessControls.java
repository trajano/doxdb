package net.trajano.doxdb.sampleejb;

import java.security.Principal;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import net.trajano.doxdb.CollectionAccessControl;
import net.trajano.doxdb.Indexer;
import net.trajano.doxdb.search.IndexView;

@Stateless
@Remote({
    CollectionAccessControl.class,
    Indexer.class
})
public class AccessControls implements
    CollectionAccessControl,
    Indexer {

    @Override
    public byte[] buildAccessKeyForCreate(final String collection,
        final String json,
        final Principal principal) {

        return "HELLO".getBytes();
    }

    @Override
    public IndexView buildIndexView(final String collection,
        final String json) {

        final IndexView indexView = new IndexView();
        indexView.setText("json", json);
        indexView.setString("collection", collection);
        indexView.setIndex("MYINDEX");
        return indexView;
    }
}
