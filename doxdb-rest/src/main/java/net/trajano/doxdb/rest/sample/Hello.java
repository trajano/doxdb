package net.trajano.doxdb.rest.sample;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/V1")
public class Hello extends Application {

    @Override
    public Set<Class<?>> getClasses() {

        return new HashSet<Class<?>>(Arrays.asList(new Class<?>[] {
            SampleDoxDbProvider.class }));
    }
}
