package net.trajano.doxdb.rest;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.Provider;

import net.trajano.doxdb.DoxID;

@Provider
public class DoxIDMapper implements
    ParamConverter<DoxID> {

    @Override
    public DoxID fromString(final String s) {

        return new DoxID(s);
    }

    @Override
    public String toString(final DoxID doxid) {

        return doxid.toString();
    }

}
