package net.trajano.doxdb.sample.test;

import net.trajano.doxdb.sampleejb.MyConfigurationProvider;
import net.trajano.doxdb.sampleejb.MyEventHandler;

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

        MyEventHandler eventHandler = new MyEventHandler();
        eventHandler.onRecordCreate("col", null, "");
        eventHandler.onRecordDelete("col", null, "");
        eventHandler.onRecordRead("col", null, "");
        eventHandler.onRecordUpdate("col", null, "");
    }

}
