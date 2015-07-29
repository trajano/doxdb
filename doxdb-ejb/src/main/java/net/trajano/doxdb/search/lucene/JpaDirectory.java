package net.trajano.doxdb.search.lucene;

import java.io.IOException;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;

public class JpaDirectory extends Directory {

    private boolean closed = false;

    /**
     * Directory name.
     */
    private final String directoryName;

    private final EntityManager em;

    private final LockFactory lockFactory;

    public JpaDirectory(final EntityManager em,
        final String directoryName) {
        this.em = em;
        this.directoryName = directoryName;
        lockFactory = new JpaLockFactory(em, directoryName);

    }

    @Override
    public void close() throws IOException {

        closed = true;

    }

    @Override
    public IndexOutput createOutput(final String name,
        final IOContext context) throws IOException {

        return new JpaIndexOutput(name, em, directoryName);

    }

    @Override
    public void deleteFile(final String name) throws IOException {

        final DoxSearchIndex indexEntry = em.createNamedQuery("searchReadOne", DoxSearchIndex.class).setParameter("directoryName", directoryName).setParameter("fileName", name).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
        em.remove(indexEntry);

    }

    /**
     * @throws AlreadyClosedException
     *             if this Directory is closed
     */
    @Override
    protected void ensureOpen() throws AlreadyClosedException {

        if (closed) {
            throw new AlreadyClosedException("closed");
        }
    }

    @Override
    public long fileLength(final String name) throws IOException {

        final DoxSearchIndex indexEntry = em.createNamedQuery("searchReadOne", DoxSearchIndex.class).setParameter("directoryName", directoryName).setParameter("fileName", name).getSingleResult();
        return indexEntry.getContentlength();
    }

    @Override
    public String[] listAll() throws IOException {

        return em.createNamedQuery("searchListAll", String.class).setParameter("directoryName", directoryName).getResultList().toArray(new String[0]);
    }

    /**
     * Creates a new lock. It does not store the lock in a map as that would
     * force this to be a singleton.
     */
    @Override
    public Lock makeLock(final String name) {

        return lockFactory.makeLock(this, name);

    }

    @Override
    public IndexInput openInput(final String name,
        final IOContext context) throws IOException {

        return new JpaIndexInput(name, em, directoryName, context);
    }

    @Override
    public void renameFile(final String source,
        final String dest) throws IOException {

        if (source.equals(dest)) {
            return;
        }

        final DoxSearchIndex indexEntry = em.createNamedQuery("searchReadOne", DoxSearchIndex.class).setParameter("directoryName", directoryName).setParameter("fileName", source).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
        indexEntry.setFileName(dest);
        em.persist(indexEntry);
    }

    /**
     * Flushes the entity manager.
     */
    @Override
    public void sync(final Collection<String> names) throws IOException {

        em.flush();
    }

}
