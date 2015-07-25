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
import net.trajano.doxdb.sample.json.Horse.Gender;
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
    public IndexView[] buildIndexViews(final String collection,
        final String json) {

        try {
            final Horse horse = new ObjectMapper().readValue(json, Horse.class);

            final IndexView indexView = new IndexView();
            indexView.setText("json", json);
            indexView.appendText(json);
            indexView.setString("collection", collection);
            indexView.setString("name", horse.getName());
            indexView.setIndex("MYINDEX");

            final IndexView indexView2 = new IndexView();
            indexView2.appendText(json);
            indexView2.setString("collection", collection);
            if (horse.getGender() == Gender.GELDING) {
                indexView2.setString("name", "*********************");
                indexView2.setText("name", horse.getName());
            } else {
                indexView2.setString("name", horse.getName());
            }
            indexView2.setIndex("MASKINDEX");

            return new IndexView[] {
                indexView,
                indexView2
            };

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
