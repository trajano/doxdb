package net.trajano.doxdb.sample.test;

import javax.json.Json;

import org.junit.Test;

import net.trajano.doxdb.DoxMeta;
import net.trajano.doxdb.internal.DoxPrincipal;
import net.trajano.doxdb.sample.ejb.MyConfigurationProvider;
import net.trajano.doxdb.sample.ejb.MyEventHandler;

/**
 * This tests instantiation of the My classes.
 *
 * @author Archimedes Trajano
 */
public class MyClassTest {

    @Test
    public void testMyConfigurationProvider() {

        new MyConfigurationProvider();
    }

    @Test
    public void testMyEventHandler() {

        final MyEventHandler eventHandler = new MyEventHandler();
        eventHandler.onRecordCreate(new DoxMeta(), "", Json.createObjectBuilder().build());
        eventHandler.onRecordDelete(new DoxMeta(), "", Json.createObjectBuilder().build());
        eventHandler.onRecordRead(new DoxPrincipal("test"), "col", null, "");
        eventHandler.onRecordUpdate(new DoxMeta(), "", Json.createObjectBuilder().build());
    }

}
