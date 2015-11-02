package net.trajano.doxdb.ext;

import java.security.Principal;

import javax.json.JsonObject;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxMeta;

/**
 * Default {@link EventHandler} that does not perform any operations.
 *
 * @author Archimedes Trajano
 */
public class DefaultEventHandler implements
    EventHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRecordCreate(final DoxMeta meta,
        final String content,
        final JsonObject extra) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRecordDelete(final DoxMeta meta,
        final String content,
        final JsonObject extra) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRecordRead(final Principal userPrincipal,
        final String collectionName,
        final DoxID doxId,
        final String json) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRecordUpdate(final DoxMeta meta,
        final String json,
        final JsonObject extra) {

    }

}
