package net.trajano.doxdb.search.lucene;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.persistence.EntityManager;

import org.apache.lucene.store.OutputStreamIndexOutput;

import net.trajano.doxdb.ejb.internal.DoxLength;

public class JpaIndexOutput extends OutputStreamIndexOutput {

    private final ByteArrayOutputStream baos;

    private final EntityManager em;

    private final DoxSearchIndex entry;

    public JpaIndexOutput(final String name,
        final EntityManager em,
        final ByteArrayOutputStream baos,
        final String directoryName) {
        super(name, baos, 8192);
        System.out.println("OUTPUT TO " + name);
        this.baos = baos;
        this.em = em;
        entry = new DoxSearchIndex();
        final DirectoryFile directoryFile = new DirectoryFile();
        directoryFile
            .setDirectoryName(directoryName);
        directoryFile.setFileName(name);
        entry.setDirectoryFile(directoryFile);

    }

    @Override
    public void close() throws IOException {

        super.close();
        baos.close();
        final byte[] buffer = baos.toByteArray();
        entry.setContent(baos.toByteArray());
        entry.setContentLength(buffer.length);
        if (buffer.length > DoxLength.INDEX_FILE_LENGTH) {
            throw new IOException();
        }
        em.persist(entry);
    }

}
