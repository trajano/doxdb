package net.trajano.doxdb;

import java.io.InputStream;
import java.security.Principal;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import net.trajano.doxdb.jdbc.JdbcDoxDAO;

import com.ibm.jbatch.container.exception.PersistenceException;

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
    @Resource(name = "doxdbDataSource")
    private XADataSource ds;

    private XAConnection connection;

    @PostConstruct
    public void init() {

        try {
            connection = ds.getXAConnection();
            dao = new JdbcDoxDAO(connection.getConnection(), buildConfiguration());
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

    public DoxID create(InputStream in,
            Principal principal) {

        return dao.create(in, principal);
    }

    public int getVersion(DoxID id) {

        return dao.getVersion(id);
    }

    public InputStream readOobContent(DoxID doxId,
            String reference) {

        return dao.readOobContent(doxId, reference);
    }

    public InputStream readContent(DoxID id) {

        return dao.readContent(id);
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

    public void attach(DoxID doxId,
            String reference,
            InputStream in,
            int version,
            Principal principal) {

        dao.attach(doxId, reference, in, version, principal);
    }

    public void updateContent(DoxID doxId,
            InputStream contentStream,
            int version,
            Principal principal) {

        dao.updateContent(doxId, contentStream, version, principal);
    }
}
