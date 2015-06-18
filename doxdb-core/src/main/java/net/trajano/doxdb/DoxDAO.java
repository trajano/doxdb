package net.trajano.doxdb;

import java.io.InputStream;
import java.security.Principal;
import java.util.Date;

public interface DoxDAO {

    DoxID create(InputStream in,
            Principal principal);

    /**
     * Imports a record directly into the data store. This will fail if the
     * record already exists.
     * 
     * @param id
     *            Dox ID
     * @param in
     *            content input stream
     * @param createdBy
     * @param createdOn
     * @param lastUpdatedBy
     * @param lastUpdatedOn
     */
    void importDox(DoxID id,
            InputStream in,
            Principal createdBy,
            Date createdOn,
            Principal lastUpdatedBy,
            Date lastUpdatedOn);

    /**
     * Get version for optimistic locking for the Dox.
     * 
     * @param id
     *            Dox ID
     * @return
     */
    int getVersion(DoxID id);

    InputStream readOobContent(DoxID doxId,
            String reference);

    InputStream readContent(DoxID id);

    void delete(DoxID id,
            int version,
            Principal principal);

    void detach(DoxID doxId,
            String reference,
            int version,
            Principal principal);

    void attach(DoxID doxId,
            String reference,
            InputStream in,
            int version,
            Principal principal);

    void updateContent(DoxID doxId,
            InputStream contentStream,
            int version,
            Principal principal);

}
