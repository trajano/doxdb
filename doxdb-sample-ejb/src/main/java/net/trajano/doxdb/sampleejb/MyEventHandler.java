package net.trajano.doxdb.sampleejb;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import net.trajano.doxdb.ext.DefaultEventHandler;

/**
 * type.
 *
 * @author Archimedes Trajano
 */
@Stateless
@Remote(net.trajano.doxdb.ext.EventHandler.class)
public class MyEventHandler extends DefaultEventHandler {

}
