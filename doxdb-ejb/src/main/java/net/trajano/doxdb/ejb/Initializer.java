package net.trajano.doxdb.ejb;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.apache.lucene.store.Lock;

import net.trajano.doxdb.ext.ConfigurationProvider;
import net.trajano.doxdb.schema.DoxPersistence;
import net.trajano.doxdb.schema.DoxType;
import net.trajano.doxdb.search.lucene.JdbcDirectory;

/**
 * This will initialize the Dox bean using an EJB that provides the Dox
 * configuration. It will also create the tables based on the configuration if
 * needed.
 *
 * @author Archimedes
 */
@Singleton
@Startup
@LocalBean
public class Initializer {

    private ConfigurationProvider configurationProvider;

    private DataSource ds;

    private void createTablesIfNeeded(final Connection c,
        final DoxType config) throws SQLException {

        final String tableName = config.getName().toUpperCase();
        if (!isTableExist(c, tableName)) {
            runTableScript(c, tableName, config.getSize().intValue(), "/META-INF/create-tables.sql");
        }

        if (config.isOob() && !isTableExist(c, tableName + "OOB")) {
            runTableScript(c, tableName, config.getOobSize().intValue(), "/META-INF/create-oob-tables.sql");
        }
    }

    /**
     * This will create the tables if needed on initialization. It forces a new
     * transaction to be created in order to prevent issues running in a cluster
     * where two instances of this bean will exist.
     */
    @PostConstruct
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void init() {

        final DoxPersistence persistenceConfig = configurationProvider.getPersistenceConfig();
        try (final Connection c = ds.getConnection()) {
            final Properties sqls = new Properties();
            try (InputStream is = getClass().getResourceAsStream("/META-INF/sqls.properties")) {
                sqls.load(is);
            }

            final Properties oobSqls = new Properties();
            try (InputStream is = getClass().getResourceAsStream("/META-INF/oob-sqls.properties")) {
                oobSqls.load(is);
            }

            for (final DoxType doxConfig : persistenceConfig.getDox()) {
                createTablesIfNeeded(c, doxConfig);

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

            try (final JdbcDirectory directory = new JdbcDirectory(c, "SEARCHINDEX")) {
                final Lock lock = directory.makeLock("write.lock");
                if (lock.isLocked()) {
                    directory.forceUnlock("write.lock");
                    System.out.println("Index was locked on startup, possible data corruption so re-indexing");
                    // TODO
                }
                lock.close();
            }

        } catch (final IOException
            | SQLException e) {
            throw new PersistenceException(e);
        }
    }

    private boolean isTableExist(final Connection c,
        final String tableName) throws SQLException {

        try (final ResultSet tables = c.getMetaData()
            .getTables(null, null, tableName, null)) {
            return tables.next();
        }
    }

    private void runTableScript(final Connection c,
        final String tableName,
        final int lobSize,
        final String sqlResource) throws SQLException {

        try (final Scanner scanner = new Scanner(getClass().getResourceAsStream(sqlResource))) {
            scanner.useDelimiter(";");

            try (final Statement stmt = c.createStatement()) {

                while (scanner.hasNext()) {
                    final String trim = String.format(scanner.next(), tableName, lobSize).replaceAll("\\s+", " ").trim();
                    if (!trim.isEmpty()) {
                        System.out.println(trim);
                        stmt.executeUpdate(trim);
                        //                        stmt.addBatch(trim);
                    }
                }
                //                stmt.executeBatch();
            }

        }

    }

    @EJB
    public void setConfigurationProvider(final ConfigurationProvider configurationProvider) {

        this.configurationProvider = configurationProvider;
    }

    @Resource
    public void setDataSource(final DataSource ds) {

        this.ds = ds;
    }

}
