package net.trajano.doxdb.rest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * Dox REST Application.
 *
 * @author Archimedes Trajano
 */
public class DoxRestApplication extends Application {

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<?>> getClasses() {

        return new HashSet<Class<?>>(Arrays.asList(DoxIDMapper.class, DoxResource.class, EntityNotFoundMapper.class, OptimisticLockingMapper.class));
    }
}
