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
 * This will be extended by the EJBs. This does not provide extension points for
 * the operations, those operations should be done on the application specific
 * versions.
 *
 * @author Archimedes
 */
public abstract class AbstractDoxDAOBean {

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

    public DoxID create(InputStream in,
            Principal principal) {

        return dao.create(in, principal);
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

    public int readContent(DoxID id,
            ByteBuffer buffer) {

        return dao.readContent(id, buffer);
    }

    public void readContentToStream(DoxID id,
            OutputStream os) throws IOException {

        dao.readContentToStream(id, os);

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

    /**
     * Closes the connection that was initialized.
     */
    @PreDestroy
    public void shutdown() {

        try {
            connection.close();
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public void updateContent(DoxID doxId,
            InputStream contentStream,
            int version,
            Principal principal) {

        dao.updateContent(doxId, contentStream, version, principal);
    }
}
