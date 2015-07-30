package net.trajano.doxdb.search.lucene;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.lucene.store.BaseDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;

public class JpaDirectory extends BaseDirectory {

    /**
     * Directory name.
     */
    private final String directoryName;

    private final EntityManager em;

    public JpaDirectory(final EntityManager em,
        final String directoryName) {
        super(new JpaLockFactory(em, directoryName));
        this.em = em;
        this.directoryName = directoryName;

    }

    @Override
    public void close() throws IOException {

        isOpen = false;

    }

    @Override
    public IndexOutput createOutput(final String name,
        final IOContext context) throws IOException {

        return new JpaIndexOutput(name, em, new ByteArrayOutputStream(), directoryName);

    }

    @Override
    public void deleteFile(final String name) throws IOException {

        System.out.println("DELETE " + name);
        em.remove(em.find(DoxSearchIndex.class, new DirectoryFile(directoryName, name)));
    }

    @Override
    public long fileLength(final String name) throws IOException {

        return em.find(DoxSearchIndex.class, new DirectoryFile(directoryName, name)).getContentLength();
    }

    @Override
    public String[] listAll() throws IOException {

        return em.createNamedQuery("searchListAll", String.class).setParameter("directoryName", directoryName).getResultList().toArray(new String[0]);
    }

    @Override
    public IndexInput openInput(final String name,
        final IOContext context) throws IOException {

        System.out.println("READ " + name);
        final DoxSearchIndex entry = em.find(DoxSearchIndex.class, new DirectoryFile(directoryName, name));
        if (entry == null) {
            return null;
        }

        return new ByteArrayIndexInput(name, context, entry.getContent());
    }

    @Override
    public void renameFile(final String source,
        final String dest) throws IOException {

        if (source.equals(dest)) {
            return;
        }

        System.out.println("RENAME " + source + " TO " + dest);
        final int c = em.createQuery("update DoxSearchIndex e set e.directoryFile.fileName = :dest where e.directoryFile.fileName = :source and e.directoryFile.directoryName = :directoryName").setParameter("directoryName", directoryName).setParameter("dest", dest).setParameter("source", source).executeUpdate();
        if (c != 1) {
            throw new PersistenceException("Rename failed");
        }

    }

    /**
     * Flushes the entity manager.
     */
    @Override
    public void sync(final Collection<String> names) throws IOException {

        em.flush();
    }

}
