package net.trajano.doxdb.search.lucene;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.persistence.EntityManager;

import org.apache.lucene.store.BufferedIndexInput;
import org.apache.lucene.store.IOContext;

public class JpaIndexInput extends BufferedIndexInput {

    private final ByteBuffer buffer;

    private final EntityManager em;

    private final DoxSearchIndex entry;

    private int pos;

    protected JpaIndexInput(final String name,
        final EntityManager em,
        final String directoryName,
        final IOContext context) {
        super(name, context);
        this.em = em;
        entry = em.createNamedQuery("searchReadOne", DoxSearchIndex.class).setParameter("directoryName", directoryName).setParameter("fileName", name).getSingleResult();
        if (entry == null) {
            buffer = ByteBuffer.allocate(0);
        } else {
            buffer = ByteBuffer.wrap(entry.getContent());
        }
        pos = 0;
    }

    @Override
    public void close() throws IOException {

        em.detach(entry);
    }

    @Override
    public long length() {

        return buffer.limit();
    }

    @Override
    protected void readInternal(final byte[] b,
        final int offset,
        final int length) throws IOException {

        System.arraycopy(buffer.array(), pos, b, offset, length);
    }

    @Override
    protected void seekInternal(final long pos) throws IOException {

        this.pos = (int) pos;

    }

}
