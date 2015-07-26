package net.trajano.doxdb.spi;

import net.trajano.doxdb.DoxID;

/**
 * Default {@link EventHandler} that does not perform any operations.
 *
 * @author Archimedes
 */
public class DefaultEventHandler implements
    EventHandler {

    @Override
    public void onRecordCreate(final String collection,
        final DoxID doxid,
        final String json) {

    }

    @Override
    public void onRecordDelete(final String collection,
        final DoxID doxid) {

    }

    @Override
    public void onRecordRead(final String collection,
        final DoxID doxid,
        final String json) {

    }

    @Override
    public void onRecordUpdate(final String collection,
        final DoxID doxid,
        final String json) {

    }

}
