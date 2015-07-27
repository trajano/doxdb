package net.trajano.doxdb.sample.test;

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;
import java.sql.Connection;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.search.lucene.JdbcDirectory;

/**
 * Tests Lucene bean.
 *
 * @author Archimedes
 */
public class LuceneTest {

    @Test
    public void testLucene() throws Exception {

        final DoxID generate = DoxID.generate();
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + generate);
        ds.setUser("sa");
        ds.setPassword("sa");
        ds.setLogWriter(new PrintWriter(System.out));

        final Connection c = ds.getConnection();
        {

            final Analyzer analyzer = new StandardAnalyzer();
            final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            final JdbcDirectory dir = new JdbcDirectory(c, "searchtable");
            final IndexWriter indexWriter = new IndexWriter(dir, iwc);

            final Document doc = new Document();
            doc.add(new StringField("name", "value", Store.YES));
            indexWriter.addDocument(doc);
            indexWriter.commit();
            indexWriter.close();
        }
        {

            final JdbcDirectory dir = new JdbcDirectory(c, "searchtable");
            final IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
            final Analyzer analyzer = new StandardAnalyzer();
            final QueryParser parser = new QueryParser("name", analyzer);
            final Query query = parser.parse("value");
            final TopDocs search = searcher.search(query, 10);
            assertEquals(1, search.totalHits);
            final Document doc = searcher.doc(search.scoreDocs[0].doc);
            assertEquals("value", doc.get("name"));
        }
    }

}
