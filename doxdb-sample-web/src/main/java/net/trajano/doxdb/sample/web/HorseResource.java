package net.trajano.doxdb.sample.web;

import java.util.Arrays;
import java.util.List;

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

    /**
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getHorses() {

        return Arrays.asList("hello", "world");
    }
}
