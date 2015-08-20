package net.trajano.doxdb.ws;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.json.Json;
import javax.websocket.Session;

import net.trajano.doxdb.DoxID;

/**
 * type.
 *
 * @author Archimedes Trajano
 */
@Singleton
@LocalBean
public class SessionManager {

    private final Set<Session> sessions = Collections.newSetFromMap(new ConcurrentHashMap<Session, Boolean>());

    @Lock(LockType.WRITE)
    public void register(final Session session) {

        sessions.add(session);
    }

    @Lock(LockType.READ)
    public void sendMessage(final String message) {

        for (final Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (final IOException e) {
                // ignore
            }
        }
    }

    @Lock(LockType.READ)
    public void sendMessage(final String operation,
        final DoxID doxID,
        final String collection,
        final Date on) {

        sendMessage(Json.createObjectBuilder().add("op", operation).add("id", doxID.toString()).add("collection", collection).add("ts", on.getTime()).build().toString());

    }

    @Lock(LockType.WRITE)
    public void unregister(final Session session) {

        sessions.remove(session);
    }

}
