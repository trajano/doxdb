package net.trajano.doxdb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.Principal;

/**
 * This will be extended by the EJBs. This does not provide extension points for
 * the operations, those operations should be done on the application specific
 * versions.
 *
 * @author Archimedes
 */
public abstract class AbstractStreamDoxDAOBean extends AbstractDoxDAOBean {

    public DoxID create(InputStream in,
            Principal principal) {

        return getDao().create(in, 1, principal);
    }

    public int readContent(DoxID id,
            ByteBuffer buffer) {

        return getDao().readContent(id, buffer);
    }

    public void readContentToStream(DoxID id,
            OutputStream os) throws IOException {

        getDao().readContentToStream(id, os);

    }

    public void updateContent(DoxID doxId,
            InputStream contentStream,
            int version,
            Principal principal) {

        getDao().updateContent(doxId, contentStream, 1, version, principal);
    }
}
