package net.trajano.doxdb.rest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
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
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxMeta;
import net.trajano.doxdb.IndexView;
import net.trajano.doxdb.SearchResult;
import net.trajano.doxdb.ejb.DoxLocal;
import net.trajano.doxdb.schema.CollectionType;
import net.trajano.doxdb.schema.LookupType;
import net.trajano.doxdb.schema.SchemaType;
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

    /**
     * Cache control when the data should not be cached.
     */
    private static final CacheControl NO_CACHE;

    /**
     * Cache control when the data can be cached. However, it should still not
     * be persisted on disk
     */
    private static final CacheControl OK_CACHE;

    /**
     * <code>application/json</code> with the the UTF-8 character set. Needs to
     * be a constant string in order to be used in annotations.
     */
    private static final String RESPONSE_TYPE = "application/json; charset=utf-8";

    static {
        NO_CACHE = new CacheControl();
        NO_CACHE.setNoCache(true);
        NO_CACHE.setNoStore(true);
        NO_CACHE.setMaxAge(-1);
        NO_CACHE.setProxyRevalidate(true);
        NO_CACHE.setSMaxAge(-1);
        NO_CACHE.setMustRevalidate(true);

        OK_CACHE = new CacheControl();
        OK_CACHE.setNoCache(false);
        OK_CACHE.setNoStore(true);
        OK_CACHE.setMaxAge(3600);
        OK_CACHE.setProxyRevalidate(true);
        OK_CACHE.setSMaxAge(-1);
        OK_CACHE.setMustRevalidate(true);
    }

    @EJB
    private DoxLocal dox;

    @EJB
    private SessionManager sessionManager;

    @POST
    @Path("search/{index}")
    @Produces(RESPONSE_TYPE)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response advancedSearch(@PathParam("index") final String index,
        final JsonObject query,
        @Context final UriInfo uriInfo) {

        final SearchResult results = dox.advancedSearch(index, query);
        final JsonObject resultJson = searchResultBuilder(uriInfo, results).build();
        return Response.ok(resultJson).cacheControl(NO_CACHE).build();
    }

    @POST
    @Path("search/{index}/{collectionName}")
    @Produces(RESPONSE_TYPE)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response advancedSearchWithinCollection(@PathParam("index") final String index,
        @PathParam("collectionName") final String schemaName,
        final JsonObject query,
        @Context final UriInfo uriInfo) {

        final SearchResult results = dox.advancedSearch(index, schemaName, query);
        final JsonObject resultJson = searchResultBuilder(uriInfo, results).build();
        return Response.ok(resultJson).cacheControl(NO_CACHE).build();
    }

    private String capitalize(final String s) {

        if (s == null || s.isEmpty()) {
            return s;
        }
        final char[] a = s.toCharArray();
        a[0] = Character.toUpperCase(a[0]);
        return new String(a);
    }

    /**
     * This will create the Dox object.
     *
     * @param collectionName
     *            collection name
     * @param content
     *            JSON content.
     * @return
     */
    @Path("{collectionName}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(RESPONSE_TYPE)
    public Response create(@PathParam("collectionName") final String collectionName,
        final JsonObject content) {

        final DoxMeta meta = dox.create(collectionName, content);
        sessionManager.sendMessage("CREATE", meta.getDoxId(), collectionName, meta.getLastUpdatedOn());
        return Response.ok(meta.getContentJson()).lastModified(meta.getLastUpdatedOn()).build();
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
    @Produces(RESPONSE_TYPE)
    public Response get(@PathParam("collection") final String collection,
        @PathParam("id") final DoxID doxid) {

        final DoxMeta meta = dox.read(collection, doxid);
        if (meta == null) {
            return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Dox not found").build();
        }
        final EntityTag entityTag = new EntityTag(String.valueOf(meta.getVersion()));
        return Response.ok(meta.getContentJson()).cacheControl(OK_CACHE).tag(entityTag).lastModified(meta.getLastUpdatedOn()).build();
    }

    /**
     * In addition to the default operations, the lookup operations will also be
     * provided as the string "getBy{lookupName}" for unique lookups. Note that
     * lookupName will have it's first character capitalized.
     *
     * @param uriInfo
     * @return
     */
    @GET
    @Path("module.js")
    @Produces("application/javascript")
    public Response getAngularModule(final @Context UriInfo uriInfo) {

        final StreamingOutput out = new StreamingOutput() {

            @Override
            public void write(final OutputStream os) throws IOException,
                WebApplicationException {

                try (final PrintStream w = new PrintStream(os)) {
                    w.println("\"use strict\";");
                    w.print("(function(){");
                    //                    w.print("function s(d){");
                    //                    w.print("delete d._id;");
                    //                    w.print("delete d._v;");
                    //                    w.print("}");
                    w.print("angular.module('doxdb',['ngResource'])");
                    for (final CollectionType collectionType : dox.getConfiguration().getDox()) {
                        final String name = collectionType.getName();
                        w.print(".factory('DoxDB" + name + "', ['$resource',function(r){");
                        final String uri = uriInfo.getBaseUriBuilder().path(name).build() + "/:id?v=:version";
                        w.print("return r('" + uri + "',{'id':'@_id','version':'@_version'},{");

                        final SchemaType currentSchemaType = collectionType.getSchema().get(collectionType.getSchema().size() - 1);

                        for (final LookupType LookupType : currentSchemaType.getUnique()) {
                            final String lookupUri = uriInfo.getBaseUriBuilder().path(name).build() + "/" + LookupType.getName() + "/:lookupKey";
                            w.print("'getBy" + capitalize(LookupType.getName()) + "':{method:'GET',url:'" + lookupUri + "'},");
                        }

                        w.print("});");
                        w.print("}])");
                    }
                    w.print(";})();");
                }
            }
        };

        return Response.ok(out).cacheControl(OK_CACHE).tag(dox.toString()).build();
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
    @Produces(RESPONSE_TYPE)
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

    /**
     * This will return either an array or a single JsobObject depending on
     * whether the lookup is for unique or not.
     *
     * @param collectionName
     *            collection name
     * @param lookupName
     *            lookup name (can be either unique or not)
     * @param lookupKey
     *            lookup key
     * @return array or a single JsobObject depending on whether the lookup is
     *         for unique or not.
     */
    @GET
    @Path("{collectionName}/{lookupName}/{lookupKey}")
    @Produces(RESPONSE_TYPE)
    public Response lookup(@PathParam("collectionName") final String collectionName,
        @PathParam("lookupName") final String lookupName,
        @PathParam("lookupKey") final String lookupKey,
        @Context final UriInfo uriInfo) {

        SchemaType schema = null;
        for (final CollectionType collection : dox.getConfiguration().getDox()) {
            if (collection.getName().equals(collectionName)) {
                schema = collection.getSchema().get(collection.getSchema().size() - 1);
                break;
            }
        }
        if (schema == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        for (final LookupType lookup : schema.getUnique()) {
            if (lookup.getName().equals(lookupName)) {
                final DoxMeta meta = dox.readByUniqueLookup(collectionName, lookupName, lookupKey);
                if (meta == null) {
                    return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Dox not found").build();
                }
                final EntityTag entityTag = new EntityTag(String.valueOf(meta.getVersion()));
                final URI location = uriInfo.getBaseUriBuilder().path(collectionName).path(meta.getDoxId().toString()).build();
                return Response.seeOther(location).cacheControl(OK_CACHE).tag(entityTag).lastModified(meta.getLastUpdatedOn()).build();
            }
        }
        for (final LookupType lookup : schema.getLookup()) {
            if (lookup.getName().equals(lookupName)) {
                return Response.ok(dox.readByLookup(collectionName, lookupName, lookupKey)).build();
            }
        }
        return Response.status(Status.NOT_FOUND).build();

    }

    private Response op(final String collection,
        final String opName,
        final JsonObject content) {

        return Response.ok().type(RESPONSE_TYPE).entity(content).build();
    }

    @GET
    @Path("{collection}")
    @Produces(RESPONSE_TYPE)
    public Response readAll(@PathParam("collection") final String collection) {

        final String readAll = dox.readAll(collection);
        if (readAll.charAt(0) == '[') {
            return Response.ok(readAll).build();
        }

        // The file is suffixed with the uncompressed size

        final File file = new File(readAll);
        final long uncompressedSize = file.length();
        final StreamingOutput out = new StreamingOutput() {

            @Override
            public void write(final OutputStream os) throws IOException,
                WebApplicationException {

                try (final InputStream fis = new BufferedInputStream(new FileInputStream(file))) {
                    int c = fis.read();
                    while (c != -1) {
                        os.write(c);
                        c = fis.read();
                    }
                } finally {
                    file.delete();
                }
            }

        };

        return Response.ok(out).cacheControl(NO_CACHE).header("Content-Encoding", "gzip").header("Content-Length", uncompressedSize).build();

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
        final JsonObject json,
        final int version) {

        final DoxMeta meta = dox.update(collection, new DoxID(id), json, version);
        sessionManager.sendMessage("UPDATE", meta.getDoxId(), collection, meta.getLastUpdatedOn());
        return Response.ok(meta.getContentJson()).lastModified(meta.getLastUpdatedOn()).build();
    }

    @POST
    @Path("{collection}/{id}/{oobname}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response saveOob(@PathParam("collection") final String collection,
        @PathParam("id") final DoxID id,
        @PathParam("oobname") final String oobname) {

        return Response.ok().type(RESPONSE_TYPE).entity("OOB").build();
    }

    @POST
    @Path("{collection}/{idOrOp}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(RESPONSE_TYPE)
    public Response saveOrOp(@PathParam("collection") final String collection,
        @PathParam("idOrOp") final String idOrOp,
        @QueryParam("v") final int version,
        final JsonObject content) {

        if (idOrOp.startsWith("_")) {
            return op(collection, idOrOp.substring(1), content);
        } else {
            return save(collection, idOrOp, content, version);
        }
    }

    /**
     * This builds the search result object except for the link to the next
     * page.
     *
     * @param uriInfo
     *            context to build the URI
     * @param results
     *            the search results from the EJB
     * @return JSON Object builder
     */
    private JsonObjectBuilder searchResultBuilder(final UriInfo uriInfo,
        final SearchResult results) {

        final JsonObjectBuilder jsonBuilder;
        final JsonArrayBuilder hitsBuilder = Json.createArrayBuilder();
        for (final IndexView hit : results.getHits()) {
            final JsonObjectBuilder hitBuilder = Json.createObjectBuilder();
            final String id = hit.getDoxID().toString();
            for (final Entry<String, BigDecimal> entry : hit.getNumbers()) {
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
        jsonBuilder = Json.createObjectBuilder().add("totalHits", results.getTotalHits()).add("hits", hitsBuilder);
        return jsonBuilder;
    }

    @GET
    @Path("search/{index}")
    @Produces(RESPONSE_TYPE)
    public Response simpleSearch(@PathParam("index") final String index,
        @QueryParam("q") final String queryString,
        @QueryParam("f") final Integer from,
        @Context final UriInfo uriInfo) {

        final SearchResult results = dox.search(index, queryString, 50, from);
        final JsonObjectBuilder resultBuilder = searchResultBuilder(uriInfo, results);
        if (results.getBottomDoc() != null) {
            final String nextPage = uriInfo.getBaseUriBuilder().path("search").path(index).queryParam("q", queryString).queryParam("f", results.getBottomDoc()).build().toASCIIString();
            resultBuilder.add("bottomDoc", results.getBottomDoc()).add("next", nextPage);
        }
        final JsonObject resultJson = resultBuilder.build();
        return Response.ok(resultJson).cacheControl(NO_CACHE).build();
    }

    @GET
    @Path("search/{index}/{schemaName}")
    @Produces(RESPONSE_TYPE)
    public Response simpleSearchWithSchemaName(@PathParam("index") final String index,
        @PathParam("schemaName") final String schemaName,
        @QueryParam("q") final String queryString,
        @QueryParam("f") final Integer from,
        @Context final UriInfo uriInfo) {

        final SearchResult results = dox.searchWithSchemaName(index, schemaName, queryString, 50, from);
        final JsonObjectBuilder resultBuilder = searchResultBuilder(uriInfo, results);
        if (results.getBottomDoc() != null) {
            final String nextPage = uriInfo.getBaseUriBuilder().path("search").path(index).path(schemaName).queryParam("q", queryString).queryParam("f", results.getBottomDoc()).build().toASCIIString();
            resultBuilder.add("bottomDoc", results.getBottomDoc()).add("next", nextPage);
        }
        final JsonObject resultJson = resultBuilder.build();
        return Response.ok(resultJson).cacheControl(NO_CACHE).build();
    }
}
