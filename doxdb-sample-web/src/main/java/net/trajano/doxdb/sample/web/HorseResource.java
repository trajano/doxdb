package net.trajano.doxdb.sample.web;

import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonArray;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.trajano.doxdb.ejb.DoxLocal;

/**
 * type.
 *
 * @author Archimedes Trajano
 */
@Path("horse")
public class HorseResource {

    @EJB
    private DoxLocal dox;

    @PersistenceContext
    private EntityManager em;

    /**
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray getHorses() {

        return Json.createArrayBuilder().add("hello").add("world").add(em.toString()).add(dox.toString()).build();
    }
}
