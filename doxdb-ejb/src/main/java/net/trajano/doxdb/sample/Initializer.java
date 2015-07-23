package net.trajano.doxdb.sample;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.sql.DataSource;

import net.trajano.doxdb.ConfigurationProvider;

/**
 * This will initialize the Dox bean using an EJB that provides the Dox
 * configuration.
 *
 * @author Archimedes
 */
@Singleton
@Startup
public class Initializer {

    @EJB
    private ConfigurationProvider configurationProvider;

    @Resource
    DataSource ds;

    @PostConstruct
    public void init() {

        System.out.println("construct=" + ds + " configurationProvider=" + configurationProvider.getPersistenceConfig().getDox());
    }
}
