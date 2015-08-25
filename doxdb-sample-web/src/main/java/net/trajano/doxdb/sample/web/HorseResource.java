package net.trajano.doxdb.sample.web;

import javax.json.Json;
import javax.json.JsonArray;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * type.
 *
 * @author Archimedes Trajano
 */
@Path("horse")
public class HorseResource {

    @PersistenceContext
    private EntityManager em;

    /**
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray getHorses() {

        return Json.createArrayBuilder().add("hello").add("world").add(em.toString()).build();
    }
}
