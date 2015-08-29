package net.trajano.doxdb.sample.ejb;

import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import net.trajano.doxdb.ext.XmlConfigurationProvider;

/**
 * The {@link XmlConfigurationProvider} should be implemented as a
 * {@link Singleton}. It can also be marked for automatic {@link Startup} so it
 * can load all the data as soon as possible.
 *
 * @author Archimedes Trajano
 */
@Singleton
@Startup
@Local(net.trajano.doxdb.ext.ConfigurationProvider.class)
public class MyConfigurationProvider extends XmlConfigurationProvider {

}
