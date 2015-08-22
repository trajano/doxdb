package net.trajano.doxdb.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.bson.BsonDocument;
import org.bson.BsonValue;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxMeta;
import net.trajano.doxdb.IndexView;
import net.trajano.doxdb.SearchResult;
import net.trajano.doxdb.ejb.DoxLocal;
import net.trajano.doxdb.ws.SessionManager;

/**
 * This provides the REST API for DoxDB. The API is built to support AngularJS
 * $resource natively.
 *
 * @author Archimedes Trajano
 */
@Path("")
@RequestScoped
public class DoxResource {

    @EJB
    private DoxLocal dox;

    @EJB
    private SessionManager sessionManager;

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
        sessionManager.sendMessage("CREATE", meta.getDoxId(), collection, meta.getLastUpdatedOn());
        return Response.ok().entity(meta.getContentJson()).lastModified(meta.getLastUpdatedOn()).build();
    }

    @DELETE
    @Path("{collection}/{id}")
    public Response delete(@PathParam("collection") final String collection,
        @PathParam("id") final DoxID doxid,
        @QueryParam("v") final int version) {

        dox.delete(collection, doxid, version);
        sessionManager.sendMessage("DELETE", doxid, collection, new Date());
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

    /**
     * Returns the schema document. It does a check to make sure each path
     * segment contains a restricted set of characters.
     *
     * @param segments
     *            path segments after the URL
     * @return the schema document.
     */
    @GET
    @Path("schema/{segments: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSchema(@PathParam("segments") final List<PathSegment> segments) {

        final UriBuilder b = UriBuilder.fromUri("schema");
        for (final PathSegment segment : segments) {
            final String pathSegment = segment.getPath();
            if (".".equals(pathSegment) || "..".equals(pathSegment) || pathSegment.endsWith(".") || !pathSegment.matches("^[-A-Za-z0-9_\\.]+$")) {
                throw new WebApplicationException("invalid request");
            }
            b.path(pathSegment);
        }
        final URI relativize = UriBuilder.fromUri("schema").build().relativize(b.build());
        if (relativize.isAbsolute()) {
            throw new WebApplicationException("invalid request");
        }

        final StreamingOutput out = new StreamingOutput() {

            @Override
            public void write(final OutputStream os) throws IOException,
                WebApplicationException {

                try (InputStream fis = dox.getSchema(relativize.toASCIIString())) {
                    if (fis == null) {
                        throw new NotFoundException();
                    }
                    int c = fis.read();
                    while (c != -1) {
                        os.write(c);
                        c = fis.read();
                    }
                }

            }
        };
        return Response.ok(out).encoding("UTF-8").build();
    }

    private Response op(final String collection,
        final String opName,
        final String content) {

        return Response.ok().entity(content).build();
    }

    @GET
    @Path("{collection}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response readAll(@PathParam("collection") final String collection) {

        final String readAll = dox.readAll(collection);
        if (readAll.charAt(0) == '[') {
            return Response.ok(readAll).build();
        }

        final StreamingOutput out = new StreamingOutput() {

            @Override
            public void write(final OutputStream os) throws IOException,
                WebApplicationException {

                try (Writer w = new OutputStreamWriter(os, "UTF-8")) {
                    try (final Reader fis = new InputStreamReader(new FileInputStream(readAll), "UTF-8")) {
                        int c = fis.read();
                        while (c != -1) {
                            w.write(c);
                            c = fis.read();
                        }
                    }
                } finally {
                    new File(readAll).delete();
                }

            }
        };
        return Response.ok(out).encoding("UTF-8").build();

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
        final String json,
        final int version) {

        final BsonDocument bson = BsonDocument.parse(json);

        final Iterator<Entry<String, BsonValue>> iterator = bson.entrySet().iterator();
        while (iterator.hasNext()) {
            final Entry<String, BsonValue> entry = iterator.next();
            if (entry.getKey().startsWith("_")) {
                iterator.remove();
            }
        }

        final DoxMeta meta = dox.update(collection, new DoxID(id), bson, version);
        sessionManager.sendMessage("UPDATE", meta.getDoxId(), collection, meta.getLastUpdatedOn());
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveOrOp(@PathParam("collection") final String collection,
        @PathParam("idOrOp") final String idOrOp,
        @QueryParam("v") final int version,
        final String content) {

        if (idOrOp.startsWith("_")) {
            return op(collection, idOrOp.substring(1), content);
        } else {
            return save(collection, idOrOp, content, version);
        }
    }

    @GET
    @Path("search/{index}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response simpleSearch(@PathParam("index") final String index,
        @QueryParam("q") final String queryString,
        @QueryParam("f") final Integer from,
        @Context final UriInfo uriInfo) {

        final SearchResult results = dox.search(index, queryString, 50, from);
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
        final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder().add("totalHits", results.getTotalHits()).add("hits", hitsBuilder);
        if (results.getBottomDoc() != null) {
            final String nextPage = uriInfo.getBaseUriBuilder().path("search").path(index).queryParam("q", queryString).queryParam("f", results.getBottomDoc()).build().toASCIIString();
            jsonBuilder.add("bottomDoc", results.getBottomDoc()).add("next", nextPage);
        }
        final JsonObject resultJson = jsonBuilder.build();
        return Response.ok().entity(resultJson).build();
    }
}
