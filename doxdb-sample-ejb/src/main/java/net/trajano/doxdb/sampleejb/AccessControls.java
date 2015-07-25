package net.trajano.doxdb.sampleejb;

import java.io.IOException;
import java.security.Principal;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.PersistenceException;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.trajano.doxdb.CollectionAccessControl;
import net.trajano.doxdb.Indexer;
import net.trajano.doxdb.Migrator;
import net.trajano.doxdb.sample.json.Horse;
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
    public byte[] buildAccessKeyForCreate(final String collection,
        final String json,
        final Principal principal) {

        return "HELLO".getBytes();
    }

    @Override
    public IndexView buildIndexView(final String collection,
        final String json) {

        try {
            final IndexView indexView = new IndexView();
            final Horse horse = new ObjectMapper().readValue(json, Horse.class);
            indexView.setText("json", json);
            indexView.appendText(json);
            indexView.setString("collection", collection);
            indexView.setString("name", horse.getName());
            indexView.setIndex("MYINDEX");
            return indexView;

        } catch (final IOException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public String migrate(final String collection,
        final int oldContentVersion,
        final int newContentVersion,
        final String json) {

        return json;
    }
}
