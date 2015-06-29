package net.trajano.doxdb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import net.trajano.doxdb.jdbc.JdbcDoxDAO;

/**
 * This provides the core capabilities for a Dox DAO bean. There are two direct
 * sub classes to this one, one for stream data and another which is specific to
 * JSON. It provides OOB streams support as well.
 *
 * @author Archimedes
 */
public abstract class AbstractDoxDAOBean implements AutoCloseable {

    private Connection connection;

    private DoxDAO dao;

    /**
     * The data source. It is required that the datasource be XA enabled so it
     * can co-exist with JPA and other operations.
     */
    @Resource
    private DataSource ds;

    public void attach(DoxID doxId,
            String reference,
            InputStream in,
            int version,
            Principal principal) {

        dao.attach(doxId, reference, in, version, principal);
    }

    /**
     * This may be overridden by classes to support other capabilities such as
     * OOB. Otherwise it will use the "simple name" of the derived class by
     * default.
     *
     * @return
     */
    protected DoxConfiguration buildConfiguration() {

        return new DoxConfiguration(getClass().getSimpleName());
    }

    /**
     * Closes the connection that was initialized.
     */

    @Override
    @PreDestroy
    public void close() throws SQLException {

        connection.close();
    }

    public void delete(DoxID id,
            int version,
            Principal principal) {

        dao.delete(id, version, principal);
    }

    public void detach(DoxID doxId,
            String reference,
            int version,
            Principal principal) {

        dao.detach(doxId, reference, version, principal);
    }

    public void exportDox(DoxID doxID,
            OutputStream os) throws IOException {

        dao.exportDox(doxID, os);

    }

    protected DoxDAO getDao() {

        return dao;
    }

    public int getVersion(DoxID id) {

        return dao.getVersion(id);
    }

    public void importDox(InputStream is) throws IOException {

        dao.importDox(is);

    }

    @PostConstruct
    public void init() {

        try {
            if (connection == null) {
                connection = ds.getConnection();
            }
            dao = new JdbcDoxDAO(connection, buildConfiguration());
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public int readOobContent(DoxID doxId,
            String reference,
            ByteBuffer buffer) {

        return dao.readOobContent(doxId, reference, buffer);
    }

    public void readOobContentToStream(DoxID id,
            String reference,
            OutputStream os) throws IOException {

        dao.readOobContentToStream(id, reference, os);

    }

    /**
     * Used for unit testing.
     *
     * @param connection
     */
    public void setConnection(Connection connection) {

        this.connection = connection;
    }
}
