package net.trajano.doxdb.ext;

import java.security.Principal;
import java.util.Map;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxMeta;

/**
 * Default {@link EventHandler} that does not perform any operations.
 *
 * @author Archimedes Trajano
 */
public class DefaultEventHandler implements
    EventHandler {

    @Override
    public void onRecordCreate(final DoxMeta meta,
        final String content,
        final Map<String, String> extra) {

    }

    @Override
    public void onRecordDelete(final DoxMeta meta,
        final String content,
        final Map<String, String> extra) {

    }

    @Override
    public void onRecordRead(final Principal userPrincipal,
        final String collectionName,
        final DoxID doxId,
        final String json) {

    }

    @Override
    public void onRecordUpdate(final DoxMeta meta,
        final String json,
        final Map<String, String> extra) {

    }

}
