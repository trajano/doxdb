package net.trajano.doxdb.sample.test;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.junit.Test;

import net.trajano.commons.testing.ResourceUtil;
import net.trajano.doxdb.search.lucene.JpaDirectory;

/**
 * Tests Lucene bean.
 *
 * @author Archimedes
 */
public class LuceneTest extends AbstractBeanTest {

    @Test
    public void testLargeLucene() throws Exception {

        tx.begin();
        {
            final Analyzer analyzer = new StandardAnalyzer();
            final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            final JpaDirectory dir = new JpaDirectory(em, "searchtable");
            final IndexWriter indexWriter = new IndexWriter(dir, iwc);

            final InputStream resourceAsStream = ResourceUtil.getResourceAsStream("MOCK_DATA.json");
            final JsonArray collection = Json.createReader(resourceAsStream).readArray();

            for (final JsonValue item : collection) {
                final JsonObject record = (JsonObject) item;

                final Document doc = new Document();
                for (final Entry<String, JsonValue> field : record.entrySet()) {
                    if (field.getValue() instanceof JsonString) {
                        doc.add(new StringField(field.getKey(), ((JsonString) field.getValue()).getString(), Store.YES));
                    }
                }
                indexWriter.addDocument(doc);
            }

            indexWriter.close();
        }
        tx.commit();
        System.err.println("========= HERE ============");
        tx.begin();
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

    @Test
    public void testLargeLucene2() throws Exception {

        final Path path = testFolder.getRoot().toPath();

        {
            final Analyzer analyzer = new StandardAnalyzer();
            final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            final Directory dir = new NIOFSDirectory(path);
            final IndexWriter indexWriter = new IndexWriter(dir, iwc);

            final InputStream resourceAsStream = ResourceUtil.getResourceAsStream("MOCK_DATA.json");
            final JsonArray collection = Json.createReader(resourceAsStream).readArray();

            for (final JsonValue item : collection) {
                final JsonObject record = (JsonObject) item;

                final Document doc = new Document();
                for (final Entry<String, JsonValue> field : record.entrySet()) {

                    if (field.getValue() instanceof JsonString) {
                        doc.add(new TextField(field.getKey(), ((JsonString) field.getValue()).getString(), Store.YES));
                    }
                }
                indexWriter.addDocument(doc);
            }

            indexWriter.close();
            dir.close();
        }

        {

            final Directory dir = new NIOFSDirectory(path);
            final IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
            final Analyzer analyzer = new StandardAnalyzer();
            final QueryParser parser = new QueryParser("last_name", analyzer);
            final Query query = parser.parse("Trajano");
            final TopDocs search = searcher.search(query, 10);
            assertEquals(1, search.totalHits);
            final Document doc = searcher.doc(search.scoreDocs[0].doc);
            assertEquals("Archimedes", doc.get("first_name"));
        }
    }

    @Test
    public void testLargeLuceneFS2() throws Exception {

        final Path path = testFolder.getRoot().toPath();

        for (int i = 0; i < 100; ++i) {
            final Analyzer analyzer = new StandardAnalyzer();
            final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            final Directory dir = new NIOFSDirectory(path);
            final IndexWriter indexWriter = new IndexWriter(dir, iwc);

            final InputStream resourceAsStream = ResourceUtil.getResourceAsStream("MOCK_DATA.json");
            final JsonArray collection = Json.createReader(resourceAsStream).readArray();

            for (final JsonValue item : collection) {
                final JsonObject record = (JsonObject) item;

                final Document doc = new Document();
                for (final Entry<String, JsonValue> field : record.entrySet()) {

                    if (field.getValue() instanceof JsonString) {
                        doc.add(new TextField(field.getKey(), ((JsonString) field.getValue()).getString(), Store.YES));
                    }
                }
                indexWriter.addDocument(doc);
            }

            indexWriter.close();
            dir.close();
        }

        {

            final Directory dir = new NIOFSDirectory(path);
            final IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
            final Analyzer analyzer = new StandardAnalyzer();
            final QueryParser parser = new QueryParser("last_name", analyzer);
            final Query query = parser.parse("Trajano");
            final TopDocs search = searcher.search(query, 10);
            assertEquals(100, search.totalHits);
            final Document doc = searcher.doc(search.scoreDocs[0].doc);
            assertEquals("Archimedes", doc.get("first_name"));
        }
    }

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
            dir.close();
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
