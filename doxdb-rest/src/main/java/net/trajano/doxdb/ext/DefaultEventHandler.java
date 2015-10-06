package net.trajano.doxdb.ext;

import java.util.Map;

import net.trajano.doxdb.DoxID;

/**
 * Default {@link EventHandler} that does not perform any operations.
 *
 * @author Archimedes Trajano
 */
public class DefaultEventHandler implements
    EventHandler {

    @Override
    public void onRecordCreate(final String collection,
        final DoxID doxid,
        final String json,
        final Map<String, String> extra) {

    }

    @Override
    public void onRecordDelete(final String collection,
        final DoxID doxid,
        final String json,
        final Map<String, String> extra) {

    }

    @Override
    public void onRecordRead(final String collection,
        final DoxID doxid,
        final String json) {

    }

    @Override
    public void onRecordUpdate(final String collection,
        final DoxID doxid,
        final String json,
        final Map<String, String> extra) {

    }

}
