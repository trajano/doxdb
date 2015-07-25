package net.trajano.doxdb.rest;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.sun.messaging.jmq.io.Status;

@Provider
public class EntityNotFoundMapper implements
    ExceptionMapper<EntityNotFoundException> {

    @Override
    public Response toResponse(final EntityNotFoundException e) {

        return Response.status(Status.NOT_FOUND).entity("Not found").build();
    }

}
