package net.trajano.doxdb.sample.test;

import java.util.Collections;

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
        eventHandler.onRecordCreate(new DoxMeta(), "", Collections.<String, String> emptyMap());
        eventHandler.onRecordDelete(new DoxMeta(), "", Collections.<String, String> emptyMap());
        eventHandler.onRecordRead(new DoxPrincipal("test"), "col", null, "");
        eventHandler.onRecordUpdate(new DoxMeta(), "", Collections.<String, String> emptyMap());
    }

}
