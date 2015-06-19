package net.trajano.doxdb;

import java.io.InputStream;
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
public abstract class AbstractDoxDAOBean implements DoxDAO {

    private DoxDAO dao;

    /**
     * The data source. It is required that the datasource be XA enabled so it
     * can co-exist with JPA and other operations.
     */
    @Resource(name = "doxdbDataSource", lookup = "java:comp/DefaultDataSource")
    private DataSource ds;

    private Connection connection;

    @PostConstruct
    public void init() {

        try {
            connection = ds.getConnection();
            dao = new JdbcDoxDAO(connection, buildConfiguration());
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Closes the connection that was initialized.
     */
    @PreDestroy
    public void shutdown() {

        try {
            connection.close();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
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

    @Override
    public DoxID create(InputStream in,
            Principal principal) {

        return dao.create(in, principal);
    }

    @Override
    public int getVersion(DoxID id) {

        return dao.getVersion(id);
    }

    @Override
    public InputStream readOobContent(DoxID doxId,
            String reference) {

        return dao.readOobContent(doxId, reference);
    }

    @Override
    public void importDox(DoxImportBuilder builder) {

        dao.importDox(builder);

    }

    @Override
    public InputStream readContent(DoxID id) {

        return dao.readContent(id);
    }

    @Override
    public void delete(DoxID id,
            int version,
            Principal principal) {

        dao.delete(id, version, principal);
    }

    @Override
    public void detach(DoxID doxId,
            String reference,
            int version,
            Principal principal) {

        dao.detach(doxId, reference, version, principal);
    }

    @Override
    public void attach(DoxID doxId,
            String reference,
            InputStream in,
            int version,
            Principal principal) {

        dao.attach(doxId, reference, in, version, principal);
    }

    @Override
    public void updateContent(DoxID doxId,
            InputStream contentStream,
            int version,
            Principal principal) {

        dao.updateContent(doxId, contentStream, version, principal);
    }
}
