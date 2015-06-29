package net.trajano.doxdb.search;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import net.trajano.doxdb.search.lucene.JdbcDirectory;

/**
 * The data is thread safe so there's no need to make this a {@link Singleton}.
 *
 * @author Archimedes
 */
public abstract class AbstractLuceneDoxSearchBean implements DoxSearch {

    private Connection connection;

    /**
     * The data source. It is required that the datasource be XA enabled so it
     * can co-exist with JPA and other operations.
     */
    @Resource(name = "doxdbDataSource", lookup = "java:comp/DefaultDataSource")
    private DataSource ds;

    protected AbstractLuceneDoxSearchBean() {

    }

    protected abstract String getSearchTableName();

    @PostConstruct
    public void init() {

        try {
            connection = ds.getConnection();

            final Analyzer analyzer = new StandardAnalyzer();
            final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            final JdbcDirectory dir = new JdbcDirectory(connection, getSearchTableName());
            final IndexWriter indexWriter = new IndexWriter(dir, iwc);

        } catch (final IOException | SQLException e) {
            throw new PersistenceException(e);
        }
    }
}
