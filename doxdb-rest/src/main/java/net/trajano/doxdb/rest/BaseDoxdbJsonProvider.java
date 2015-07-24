package net.trajano.doxdb.rest;

import javax.annotation.ManagedBean;
import javax.ejb.EJB;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.trajano.doxdb.Dox;
import net.trajano.doxdb.DoxID;

/**
 * This class is extended by clients to provide a list of objects that are
 * allowed and allowed OOB references along with their schema.
 * <p>
 * Registration is done through a list of schemas provided by the
 * {@link #getRegisteredSchemaResources()} method. The DOXDB table that gets
 * created would be based on the the "$doxdb" object that is required in the
 * schema. In the future an alternate version of this provider will allow
 * passing in the contents of "$doxdb" only with a reference to a schema.
 * <p>
 * Given the following, there's no need for a "customized" WAR file for the REST
 * API. However, there still is a need actually... in case we want the web
 * resources in the same WAR... or perhaps we just have it as a separate web
 * module? Let's try separate web module first.
 *
 * <pre>
 * GET {collection}/{id : [A-Za-z0-9]{32} }
 * GET {collection}/{id : [A-Za-z0-9]{32} }/{oobname}
 * POST {collection}
 * POST {collection}/{id : [A-Za-z0-9]{32} }
 * POST {collection}/{id : [A-Za-z0-9]{32} }/{oobname}
 * POST {collection}/{operation : _[A-Za-z0-9]+}
 * DELETE {collection}/{id : [A-Za-z0-9]{32} }
 * </pre>
 *
 * @author Archimedes
 */
@Path("")
@ManagedBean
public class BaseDoxdbJsonProvider {

    @EJB(beanInterface = Dox.class)
    private Dox dox;

    @Path("{collection}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("collection") final String collection,
        final JsonObject content) {

        final String savedJson = dox.create(collection, content.toString());
        return Response.ok().entity(savedJson).build();
    }

    @DELETE
    @Path("{collection}/{id : [A-Za-z0-9]{32} }")
    public Response delete(@PathParam("collection") final String collection,
        @PathParam("id") final String id) {

        // DAO.
        return Response.ok().entity(collection + " " + id).build();
    }

    @GET
    @Path("{collection}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("collection") final String collection,
        @PathParam("id") final String id) {

        return Response.ok().entity(dox.read(collection, new DoxID(id))).build();
    }

    private Response op(final String collection,
        final String opName,
        final JsonObject content) {

        System.out.println(collection + " " + opName + " SAVE");
        return Response.ok().entity(content).build();
    }

    @GET
    @Path("hello")
    public Response ping() {

        System.out.println("dox=" + dox);
        dox.noop();
        return Response.ok().entity("dox= " + dox).build();
    }

    private Response save(final String collection,
        final String id,
        final JsonObject content) {

        System.out.println(collection + " " + id + " SAVE");
        return Response.ok().entity(content).build();
    }

    @POST
    @Path("{collection}/{idOrOp}")
    public Response saveOrOp(@PathParam("collection") final String collection,
        @PathParam("idOrOp") final String idOrOp,
        final JsonObject content) {

        if (idOrOp.startsWith("_")) {
            return op(collection, idOrOp.substring(1), content);
        } else {
            return save(collection, idOrOp, content);
        }
    }

}
