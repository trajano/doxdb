package net.trajano.doxdb.sample.ejb;

import java.io.IOException;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.PersistenceException;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.trajano.doxdb.IndexView;
import net.trajano.doxdb.ext.CollectionAccessControl;
import net.trajano.doxdb.ext.Indexer;
import net.trajano.doxdb.ext.Migrator;
import net.trajano.doxdb.sample.json.Horse;
import net.trajano.doxdb.sample.json.Horse.Gender;
import net.trajano.doxdb.sample.json.Venue;

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

        try {
            if ("horse".equals(collection)) {
                final Horse horse = new ObjectMapper().readValue(json, Horse.class);

                final IndexView indexView = new IndexView();
                indexView.setText("json", json);
                indexView.appendText(json);
                indexView.setString("collection", collection);
                indexView.setString("name", horse.getName());
                indexView.setIndex("myindex");

                final IndexView indexView2 = new IndexView();
                indexView2.appendText(json);
                indexView2.setString("collection", collection);
                if (horse.getGender() == Gender.GELDING) {
                    indexView2.setString("name", "*********************");
                    indexView2.setText("name", horse.getName());
                } else {
                    indexView2.setString("name", horse.getName());
                }
                indexView2.setIndex("maskedindex");

                return new IndexView[] {
                    indexView,
                    indexView2
                };
            } else if ("venue".equals(collection)) {
                final Venue venue = new ObjectMapper().readValue(json, Venue.class);

                final IndexView indexView = new IndexView();
                indexView.setText("json", json);
                indexView.appendText(json);
                indexView.setString("collection", collection);
                indexView.setString("name", venue.getName());
                indexView.setNumber("time", System.currentTimeMillis());
                indexView.setNumber("float", 156.6 / System.currentTimeMillis());
                indexView.setIndex("myindex");

                final IndexView indexView2 = new IndexView();
                indexView2.appendText(json);
                indexView2.setString("collection", collection);
                if ("en".equals(venue.getLanguage())) {
                    indexView2.setString("name", "*********************");
                    indexView2.setText("name", venue.getName());
                } else {
                    indexView2.setString("name", venue.getName());
                }
                indexView2.setIndex("maskedindex");

                return new IndexView[] {
                    indexView,
                    indexView2
                };
            } else {
                return new IndexView[0];
            }

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
