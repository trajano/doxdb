package net.trajano.doxdb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.Principal;

public interface DoxDAO {

    void attach(DoxID doxId,
            String reference,
            InputStream in,
            int version,
            Principal principal);

    DoxID create(InputStream in,
            Principal principal);

    void delete(DoxID id,
            int version,
            Principal principal);

    void detach(DoxID doxId,
            String reference,
            int version,
            Principal principal);

    /**
     * Writes the complete Dox record including OOB data to an output stream.
     * The format is compatible for {@link #importDox(InputStream)}.
     *
     * @param doxID
     *            Dox ID
     * @param os
     * @throws IOException
     */
    void exportDox(DoxID doxID,
            OutputStream os) throws IOException;

    /**
     * Get version for optimistic locking for the Dox.
     *
     * @param id
     *            Dox ID
     * @return
     */
    int getVersion(DoxID id);

    /**
     * Imports a record directly into the data store. This will fail if the
     * record already exists.
     *
     * @param is
     *            input stream containing the data for the import.
     * @throws IOException
     */
    void importDox(InputStream is) throws IOException;

    /**
     * Reads content into a buffer. This does not return an {@link InputStream}
     * because the source would be closed.
     *
     * @param doxId
     * @param buffer
     *            buffer to write the data to
     * @return size of data that was read
     */
    int readContent(DoxID id,
            ByteBuffer buffer);

    /**
     * Reads content into an output stream.
     *
     * @param doxId
     * @param os
     *            output stream
     */
    void readContentToStream(DoxID id,
            OutputStream os) throws IOException;

    /**
     * Reads OOB content into a buffer.
     *
     * @param doxId
     * @param reference
     *            OOB reference
     * @param buffer
     *            buffer to write the data to
     * @return size of data that was read
     */
    int readOobContent(DoxID doxId,
            String reference,
            ByteBuffer buffer);

    /**
     * Reads content into an output stream.
     *
     * @param doxId
     * @param reference
     *            OOB reference
     * @param os
     *            output stream
     */
    void readOobContentToStream(DoxID id,
            String reference,
            OutputStream os) throws IOException;

    void updateContent(DoxID doxId,
            InputStream contentStream,
            int version,
            Principal principal);

}
