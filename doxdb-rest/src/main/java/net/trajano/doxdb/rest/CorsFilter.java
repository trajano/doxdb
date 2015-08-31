package net.trajano.doxdb.rest;

import java.io.IOException;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import net.trajano.doxdb.ejb.DoxLocal;

/**
 * type.
 *
 * @author Archimedes Trajano
 */
@RequestScoped
public class CorsFilter implements
    ContainerResponseFilter {

    @EJB
    private DoxLocal dox;

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ContainerRequestContext req,
        final ContainerResponseContext resp) throws IOException {

        if (dox.getConfiguration().isSetCors()) {
            resp.getHeaders().add("Access-Control-Allow-Origin", dox.getConfiguration().getCors());
        }
    }

}
