package net.trajano.doxdb.sample.test;

import net.trajano.doxdb.sample.ejb.MyConfigurationProvider;
import net.trajano.doxdb.sample.ejb.MyEventHandler;

/**
 * This tests instantiation of the My classes.
 *
 * @author Archimedes Trajano
 */
public class MyClassTest {

    public void testMyConfigurationProvider() {

        new MyConfigurationProvider();
    }

    public void testMyEventHandler() {

        final MyEventHandler eventHandler = new MyEventHandler();
        eventHandler.onRecordCreate("col", null, "");
        eventHandler.onRecordDelete("col", null, "");
        eventHandler.onRecordRead("col", null, "");
        eventHandler.onRecordUpdate("col", null, "");
    }

}
