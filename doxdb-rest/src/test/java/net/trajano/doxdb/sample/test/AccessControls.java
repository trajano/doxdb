package net.trajano.doxdb.sample.test;

import java.security.Principal;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.json.JsonObject;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxMeta;
import net.trajano.doxdb.IndexView;
import net.trajano.doxdb.ext.CollectionAccessControl;
import net.trajano.doxdb.ext.EventHandler;
import net.trajano.doxdb.ext.Indexer;
import net.trajano.doxdb.ext.Migrator;

@Stateless
@Remote({
    CollectionAccessControl.class,
    Indexer.class,
    EventHandler.class,
    Migrator.class
})
public class AccessControls implements
    CollectionAccessControl,
    EventHandler,
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

    @Override
    public void onRecordCreate(final DoxMeta meta,
        final String json,
        final JsonObject extra) {

        System.out.println("created " + meta.getCollectionName() + " " + meta.getDoxId() + " " + json);
    }

    @Override
    public void onRecordDelete(final DoxMeta meta,
        final String json,
        final JsonObject extra) {

        System.out.println("deleted " + meta.getCollectionName() + " " + meta.getDoxId() + " " + json);

    }

    @Override
    public void onRecordRead(final Principal principal,
        final String collection,
        final DoxID doxid,
        final String json) {

        System.out.println("read " + collection + " " + doxid + " " + json);

    }

    @Override
    public void onRecordUpdate(final DoxMeta meta,
        final String json,
        final JsonObject extra) {

        System.out.println("updated " + meta.getCollectionName() + " " + meta.getDoxId() + " " + json);

    }
}
