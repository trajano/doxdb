package net.trajano.doxdb.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map.Entry;

import javax.annotation.ManagedBean;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.OptimisticLockException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonNumber;
import org.bson.BsonValue;

import net.trajano.doxdb.Dox;
import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxMeta;
import net.trajano.doxdb.IndexView;
import net.trajano.doxdb.SearchResult;

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
 * GET search/{index}?q={query}
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
public class DoxResource {

    @EJB(beanInterface = Dox.class)
    private Dox dox;

    @Path("{collection}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("collection") final String collection,
        final String json) {

        final BsonDocument bson = BsonDocument.parse(json);
        for (final String key : bson.keySet()) {
            if (key.startsWith("_")) {
                bson.remove(key);
            }
        }

        final DoxMeta meta = dox.create(collection, bson);
        return Response.ok().entity(meta.getContentJson()).lastModified(meta.getLastUpdatedOn()).build();
    }

    @DELETE
    @Path("{collection}/{id}")
    public Response delete(@PathParam("collection") final String collection,
        @PathParam("id") final DoxID doxid,
        final JsonObject json) {

        dox.delete(collection, doxid, json.getInt("_version"));
        return Response.noContent().build();
    }

    @GET
    @Path("{collection}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("collection") final String collection,
        @PathParam("id") final DoxID doxid) {

        final DoxMeta meta = dox.read(collection, doxid);
        if (meta == null) {
            return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Dox not found").build();
        }
        final EntityTag entityTag = new EntityTag(String.valueOf(meta.getVersion()));
        return Response.ok().tag(entityTag).entity(meta.getContentJson()).lastModified(meta.getLastUpdatedOn()).build();
    }

    private Response op(final String collection,
        final String opName,
        final String content) {

        System.out.println(collection + " " + opName + " SAVE");
        return Response.ok().entity(content).build();
    }

    @GET
    @Path("{collection}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response readAll(@PathParam("collection") final String collection) {

        // FIXME this is so yucky.
        final BsonArray all = dox.readAll(collection);
        if (all.isEmpty()) {
            return Response.ok("[]").build();
        }
        final StreamingOutput stream = new StreamingOutput() {

            @Override
            public void write(final OutputStream os) throws IOException,
                WebApplicationException {

                final Writer writer = new OutputStreamWriter(os);
                writer.write('[');
                for (int i = 0; i < all.size() - 1; ++i) {
                    writer.write(all.get(i).asDocument().toJson());
                    writer.write(',');

                }
                writer.write(all.get(all.size() - 1).asDocument().toJson());
                writer.write(']');
                writer.flush();
            }
        };

        return Response.ok(stream).build();
    }

    @OPTIONS
    @Path("reindex")
    public Response reindex() {

        // TODO remove this later.
        dox.reindex();
        return Response.noContent().build();
    }

    private Response save(final String collection,
        final String id,
        final String json) {

        final BsonDocument bson = BsonDocument.parse(json);

        final BsonValue removed = bson.remove("_version");
        if (removed == null) {
            throw new OptimisticLockException("Missing version");
        }
        for (final String key : bson.keySet()) {
            if (key.startsWith("_")) {
                bson.remove(key);
            }
        }
        final int version = ((BsonNumber) removed).intValue();

        final DoxMeta meta = dox.update(collection, new DoxID(id), bson, version);
        return Response.ok().entity(meta.getContentJson()).lastModified(meta.getLastUpdatedOn()).build();
    }

    @POST
    @Path("{collection}/{id}/{oobname}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response saveOob(@PathParam("collection") final String collection,
        @PathParam("id") final DoxID id,
        @PathParam("oobname") final String oobname) {

        return Response.ok().entity("OOB").build();
    }

    @POST
    @Path("{collection}/{idOrOp}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveOrOp(@PathParam("collection") final String collection,
        @PathParam("idOrOp") final String idOrOp,
        final String content) {

        if (idOrOp.startsWith("_")) {
            return op(collection, idOrOp.substring(1), content);
        } else {
            return save(collection, idOrOp, content);
        }
    }

    @GET
    @Path("search/{index}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response simpleSearch(@PathParam("index") final String index,
        @QueryParam("q") final String queryString,
        @Context final UriInfo uriInfo) {

        final SearchResult results = dox.search(index, queryString, 50);
        //results.get
        final JsonArrayBuilder hitsBuilder = Json.createArrayBuilder();
        for (final IndexView hit : results.getHits()) {
            final JsonObjectBuilder hitBuilder = Json.createObjectBuilder();
            final String id = hit.getDoxID().toString();
            for (final Entry<String, Double> entry : hit.getDoubles()) {
                hitBuilder.add(entry.getKey(), entry.getValue());
            }
            for (final Entry<String, Long> entry : hit.getLongs()) {
                hitBuilder.add(entry.getKey(), entry.getValue());
            }
            for (final Entry<String, String> entry : hit.getStrings()) {
                hitBuilder.add(entry.getKey(), entry.getValue());
            }
            hitBuilder.add("_collection", hit.getCollection());
            if (!hit.isMasked()) {
                hitBuilder.add("_id", id);
                hitBuilder.add("_url", uriInfo.getBaseUriBuilder().path(hit.getCollection()).path(id).build().toString());
            }
            hitsBuilder.add(hitBuilder);
        }
        final JsonObject resultJson = Json.createObjectBuilder().add("totalHits", results.getTotalHits()).add("hits", hitsBuilder).build();
        return Response.ok().entity(resultJson).build();
    }
}
