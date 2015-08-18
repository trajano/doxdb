package net.trajano.doxdb.ws;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * <p>
 * Manages the notification connections for doxdb. This class does not handle
 * any messages. As such the following method is not present although a future
 * version may accept messages to "filter" out the notifications so they only
 * get the ones they are interested in.
 * </p>
 *
 * <pre>
 *
 * &#64;OnMessage
 * public String handleMessage(final Session session,
 *     final String message) {
 *
 *     return message;
 * }
 * </pre>
 *
 * @author Archimedes Trajano
 */
@ApplicationScoped
@ServerEndpoint("/doxdb")
public class DoxNotification {

    @EJB
    private SessionManager sessionManager;

    @OnClose
    public void closeSession(final Session session,
        final CloseReason reason) {

        sessionManager.unregister(session);

    }

    @OnOpen
    public void openSession(final Session session,
        final EndpointConfig endpointConfig) {

        sessionManager.register(session);

    }

}
