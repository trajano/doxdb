package net.trajano.doxdb.rest;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.bson.BsonDocument;
import org.bson.BsonNumber;
import org.bson.BsonValue;

import net.trajano.doxdb.DocumentMeta;
import net.trajano.doxdb.Dox;
import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxSearch;
import net.trajano.doxdb.search.IndexView;
import net.trajano.doxdb.search.SearchResult;

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

    @EJB(beanInterface = DoxSearch.class)
    private DoxSearch doxSearch;

    @Path("{collection}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("collection") final String collection,
        final String json) {

        final BsonDocument bson = BsonDocument.parse(json);
        bson.remove("_id");
        bson.remove("_version");

        final DocumentMeta meta = dox.create(collection, bson);
        return Response.ok().entity(meta.getContentJson()).lastModified(meta.getLastUpdatedOn()).build();
    }

    @DELETE
    @Path("{collection}/{id}")
    public Response delete(@PathParam("collection") final String collection,
        @PathParam("id") final DoxID doxid,
        @QueryParam("v") final int version) {

        dox.delete(collection, doxid, version);
        return Response.noContent().build();
    }

    @GET
    @Path("{collection}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("collection") final String collection,
        @PathParam("id") final DoxID doxid) {

        final DocumentMeta meta = dox.read(collection, doxid);
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
    @Path("hello")
    public Response ping() {

        System.out.println("dox=" + dox);
        dox.noop();
        return Response.ok().entity("dox= " + dox).build();
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
        bson.remove("_id");
        final BsonValue removed = bson.remove("_version");
        if (removed == null) {
            throw new OptimisticLockException();
        }
        final int version = ((BsonNumber) removed).intValue();

        final DocumentMeta meta = dox.update(collection, new DoxID(id), bson, version);
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

        final SearchResult results = doxSearch.search(index, queryString, 50);
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
