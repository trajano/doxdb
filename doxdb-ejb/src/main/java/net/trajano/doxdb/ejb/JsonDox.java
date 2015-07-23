package net.trajano.doxdb.ejb;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import net.trajano.doxdb.ConfigurationProvider;
import net.trajano.doxdb.Dox;
import net.trajano.doxdb.schema.DoxPersistence;
import net.trajano.doxdb.schema.DoxType;

/**
 * This will be an SLSB. There should be many instances of this and should be
 * able to spread through the EJB pool.
 *
 * @author trajanar
 */
@Stateless
public class JsonDox implements
    Dox {

    @EJB
    private ConfigurationProvider configurationProvider;

    @Resource
    private DataSource ds;

    /**
     * Time the initialization happened.
     */
    private transient long initTimeInMillis;

    private transient Properties oobSqls = new Properties();

    private transient DoxPersistence persistenceConfig;

    private transient Properties sqls = new Properties();

    /**
     * Logs the destruction of the EJB. This is used to determine if the pool
     * size may need to be increased or not. In Wildfly, session bean pooling is
     * disabled by default and will incur significant performance loss.
     */
    @PreDestroy
    public void cleanup() {

        System.out.println("Uninitializing dox " + this);
        if (System.currentTimeMillis() - initTimeInMillis < 10 * 1000) {
            System.out.println("EJB " + this + " was deallocated in less than 10 seconds, check the stateless session pool size value.");
        }

    }

    /**
     * This will initialize the EJB and prepare the statements used by the
     * framework. It will create the tables as needed.
     */
    @PostConstruct
    public void init() {

        try (InputStream is = getClass().getResourceAsStream("/META-INF/sqls.properties")) {
            sqls.load(is);
        } catch (final IOException e) {
            throw new PersistenceException(e);
        }
        try (InputStream is = getClass().getResourceAsStream("/META-INF/oob-sqls.properties")) {
            oobSqls.load(is);
        } catch (final IOException e) {
            throw new PersistenceException(e);
        }
        persistenceConfig = configurationProvider.getPersistenceConfig();

        try (final Connection c = ds.getConnection()) {

            for (final DoxType doxConfig : persistenceConfig.getDox()) {
                for (final Object sql : sqls.values()) {
                    final PreparedStatement stmt = c.prepareStatement(String.format(sql.toString(), doxConfig.getName().toUpperCase()));
                    stmt.close();
                }
                if (doxConfig.isOob()) {
                    for (final Object sql : oobSqls.values()) {
                        final PreparedStatement stmt = c.prepareStatement(String.format(sql.toString(), doxConfig.getName().toUpperCase()));
                        stmt.close();
                    }
                }
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
        initTimeInMillis = System.currentTimeMillis();
    }

    @Override
    public void noop() {

    }

}
