package net.trajano.doxdb.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.PersistenceException;

import net.trajano.doxdb.DoxConfiguration;
import net.trajano.doxdb.DoxDAO;
import net.trajano.doxdb.DoxFactory;

/**
 * Like an Entity Manager Factory, this is a singleton in relation to an
 * application. It is expected that an EJB jar will load this through the
 * ejb-jar.xml file. The DoxFactory will create the necessary tables.
 *
 * @author Archimedes
 */
public class JdbcDoxFactory implements
    DoxFactory {

    private final Connection c;

    final Map<String, JdbcDoxDAO> doxen = new ConcurrentHashMap<>();

    public JdbcDoxFactory(final Connection c,
        final String... doxNames) {
        this.c = c;
        for (final String doxName : doxNames) {
            final DoxConfiguration configuration = new DoxConfiguration();
            configuration.setTableName(doxName);
            configuration.setHasOob(true);
            doxen.put(doxName, new JdbcDoxDAO(c, configuration));
        }
    }

    @Override
    public void close() {

        try {
            // close all the doxens
            c.close();
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public DoxDAO getDox(final String name) {

        return doxen.get(name);
    }

}
