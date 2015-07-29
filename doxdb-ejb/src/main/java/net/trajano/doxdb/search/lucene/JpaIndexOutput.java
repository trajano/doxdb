package net.trajano.doxdb.search.lucene;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;

import org.apache.lucene.store.IndexOutput;

public class JpaIndexOutput extends IndexOutput {

    /**
     * At the moment this is the most portable approach at the expense of memory
     * usage spikes.
     */
    private final ByteArrayOutputStream baos;

    private final Checksum digest;

    private DoxSearchIndex entry;

    private long pos;

    public JpaIndexOutput(final String name,
        final EntityManager em,
        final String directoryName) {
        super(name);

        try {
            entry = em.createNamedQuery("searchReadOne", DoxSearchIndex.class).setParameter("directoryName", directoryName).setParameter("fileName", name).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
        } catch (final NoResultException e) {
            entry = new DoxSearchIndex();
            entry.setContent(new byte[0]);
            entry.setContentlength(0);
            entry.setDirectoryName(directoryName);
            entry.setFileName(name);
            em.persist(entry);
        }
        pos = 0;
        digest = new CRC32();
        baos = new ByteArrayOutputStream();

    }

    @Override
    public void close() throws IOException {

        final byte[] buffer = baos.toByteArray();
        entry.setContent(baos.toByteArray());
        entry.setContentlength(buffer.length);
    }

    @Override
    public long getChecksum() throws IOException {

        return digest.getValue();
    }

    @Override
    public long getFilePointer() {

        return pos;
    }

    @Override
    public void writeByte(final byte b) throws IOException {

        baos.write(b);
        ++pos;
        digest.update(b);

    }

    @Override
    public void writeBytes(final byte[] buffer,
        final int offset,
        final int length) throws IOException {

        baos.write(buffer, offset, length);
        digest.update(buffer, offset, length);
        pos += length;
    }

}
