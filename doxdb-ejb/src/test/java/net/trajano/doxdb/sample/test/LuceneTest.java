package net.trajano.doxdb.sample.test;

import static org.junit.Assert.assertEquals;

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
import org.junit.Test;

import net.trajano.doxdb.search.lucene.JpaDirectory;

/**
 * Tests Lucene bean.
 *
 * @author Archimedes
 */
public class LuceneTest extends AbstractBeanTest {

    @Test
    public void testLucene() throws Exception {

        tx.begin();
        {

            final Analyzer analyzer = new StandardAnalyzer();
            final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            final JpaDirectory dir = new JpaDirectory(em, "searchtable");
            final IndexWriter indexWriter = new IndexWriter(dir, iwc);

            final Document doc = new Document();
            doc.add(new StringField("name", "value", Store.YES));
            indexWriter.addDocument(doc);
            indexWriter.commit();
            indexWriter.close();
        }
        {

            final JpaDirectory dir = new JpaDirectory(em, "searchtable");
            final IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
            final Analyzer analyzer = new StandardAnalyzer();
            final QueryParser parser = new QueryParser("name", analyzer);
            final Query query = parser.parse("value");
            final TopDocs search = searcher.search(query, 10);
            assertEquals(1, search.totalHits);
            final Document doc = searcher.doc(search.scoreDocs[0].doc);
            assertEquals("value", doc.get("name"));
        }
        tx.commit();
    }

}
