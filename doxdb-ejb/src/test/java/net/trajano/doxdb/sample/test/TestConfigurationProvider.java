package net.trajano.doxdb.sample.test;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import net.trajano.doxdb.ConfigurationProvider;
import net.trajano.doxdb.jdbc.XmlConfigurationProvider;

@Stateless
@Remote(ConfigurationProvider.class)
public class TestConfigurationProvider extends XmlConfigurationProvider {

}
