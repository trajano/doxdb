package net.trajano.doxdb.sample.ejb;

import javax.ejb.Local;
import javax.ejb.Stateless;

import net.trajano.doxdb.ext.DefaultEventHandler;

/**
 * type.
 *
 * @author Archimedes Trajano
 */
@Stateless
@Local(net.trajano.doxdb.ext.EventHandler.class)
public class MyEventHandler extends DefaultEventHandler {

}
