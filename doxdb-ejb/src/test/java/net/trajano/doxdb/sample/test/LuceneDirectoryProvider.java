package net.trajano.doxdb.sample.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.ibm.jbatch.container.exception.PersistenceException;

import net.trajano.doxdb.Dox;

@Singleton
@Startup
public class LuceneDirectoryProvider {

    @EJB
    private Dox doxBean;

    private Path path;

    @PostConstruct
    public void init() {

        try {
            path = Files.createTempDirectory("lucene");
            doxBean.reindex();
        } catch (final IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public Directory provideDirectory() {

        try {
            return FSDirectory.open(path);
        } catch (final IOException e) {
            throw new PersistenceException(e);
        }
    }
}
