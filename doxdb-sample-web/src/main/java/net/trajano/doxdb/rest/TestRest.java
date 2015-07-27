package net.trajano.doxdb.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * type.
 *
 * @author Archimedes Trajano
 */
@Path("")
public class TestRest {

    @GET
    public String hello() {

        return "hello";
    }
}
