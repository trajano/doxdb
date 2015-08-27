package net.trajano.doxdb.sample.ejb;

import javax.ejb.Local;
import javax.ejb.Stateless;

import net.trajano.doxdb.ext.XmlConfigurationProvider;

/**
 * type.
 *
 * @author Archimedes Trajano
 */
@Stateless
@Local(net.trajano.doxdb.ext.ConfigurationProvider.class)
public class MyConfigurationProvider extends XmlConfigurationProvider {

}
