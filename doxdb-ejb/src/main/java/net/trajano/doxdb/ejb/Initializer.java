package net.trajano.doxdb.ejb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import net.trajano.doxdb.ext.ConfigurationProvider;
import net.trajano.doxdb.schema.DoxType;

/**
 * This will initialize the Dox bean using an EJB that provides the Dox
 * configuration. It will also create the tables based on the configuration if
 * needed.
 *
 * @author Archimedes
 */
@Singleton
@Startup
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

        try (final Connection c = ds.getConnection()) {
            for (final DoxType doxConfig : configurationProvider.getPersistenceConfig().getDox()) {
                createTablesIfNeeded(c, doxConfig);
            }
        } catch (final SQLException e) {
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
                    final String trim = String.format(scanner.next(), tableName, lobSize).trim();
                    if (!trim.isEmpty()) {
                        stmt.addBatch(trim);
                    }
                }
                stmt.executeBatch();
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
