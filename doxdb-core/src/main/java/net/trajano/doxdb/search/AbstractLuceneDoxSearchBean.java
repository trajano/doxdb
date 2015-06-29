package net.trajano.doxdb.search;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import net.trajano.doxdb.search.lucene.JdbcDirectory;

/**
 * Implementers of this class do not require the {@link Singleton} annotation.
 *
 * @author Archimedes
 */
public abstract class AbstractLuceneDoxSearchBean implements DoxSearch, Closeable, AutoCloseable {

    private Connection connection;

    /**
     * The data source. It is required that the datasource be XA enabled so it
     * can co-exist with JPA and other operations.
     */
    @Resource
    private DataSource ds;

    private transient IndexSearcher indexSearcher;

    /**
     * Maps directories to
     */
    private transient IndexWriter indexWriter;

    protected AbstractLuceneDoxSearchBean() {

    }

    @Override
    public void addToIndex(IndexView indexView) {

        try {
            final Document doc = buildFromIndexView(indexView);
            indexWriter.updateDocument(new Term("_id", indexView.getDoxID()
                    .toString()), doc);
        } catch (final IOException e) {
            throw new PersistenceException(e);
        }
    }

    private IndexView buildFromDoc(Document doc) {

        final IndexView ret = new IndexView();
        for (final IndexableField field : doc.getFields()) {
            if ("_index".equals(field.name())) {
                ret.setIndex(field.stringValue());
            } else if (field instanceof StringField) {
                ret.setString(field.name(), field.stringValue());
            } else if (field instanceof DoubleField) {
                ret.setDouble(field.name(), ((DoubleField) field).numericValue()
                        .doubleValue());
            } else if (field instanceof LongField) {
                ret.setLong(field.name(), ((LongField) field).numericValue()
                        .longValue());
            }
        }
        return ret;

    }

    private Document buildFromIndexView(IndexView indexView) {

        final Document doc = new Document();
        doc.add(new StringField("_index", indexView.getIndex(), Store.YES));
        doc.add(new StringField("_id", indexView.getDoxID()
                .toString(), Store.YES));
        for (final Entry<String, String> entry : indexView.getStrings()) {
            doc.add(new StringField(entry.getKey(), entry.getValue(), Store.YES));
        }
        for (final Entry<String, String> entry : indexView.getTexts()) {
            doc.add(new TextField(entry.getKey(), entry.getValue(), Store.NO));
        }
        for (final Entry<String, Double> entry : indexView.getDoubles()) {
            doc.add(new DoubleField(entry.getKey(), entry.getValue(), Store.YES));
        }
        for (final Entry<String, Long> entry : indexView.getLongs()) {
            doc.add(new LongField(entry.getKey(), entry.getValue(), Store.YES));
        }
        doc.add(new TextField("_text", indexView.getText(), Store.NO));
        return doc;

    }

    private SearchResult buildSearchResults(TopDocs search) throws IOException {

        final SearchResult result = new SearchResult();
        result.setTotalHits(search.totalHits);
        for (final ScoreDoc scoreDoc : search.scoreDocs) {
            final Document doc = indexSearcher.doc(scoreDoc.doc);

            result.addHit(buildFromDoc(doc));
        }
        return result;
    }

    @Override
    @PreDestroy
    public void close() {

        try {
            indexWriter.close();
            connection.close();
        } catch (SQLException | IOException e) {
            throw new PersistenceException(e);
        }
    }

    protected abstract String getSearchTableName();

    @PostConstruct
    public void init() {

        try {
            connection = ds.getConnection();

            final Analyzer analyzer = new StandardAnalyzer();
            final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            final JdbcDirectory dir = new JdbcDirectory(connection, getSearchTableName());
            indexWriter = new IndexWriter(dir, iwc);
            indexSearcher = new IndexSearcher(DirectoryReader.open(indexWriter, true));
        } catch (final IOException | SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public SearchResult search(String index,
            String queryString,
            int limit) {

        try {
            final Analyzer analyzer = new StandardAnalyzer();

            final QueryParser parser = new QueryParser("_text", analyzer);
            final Query query = parser.parse(queryString);
            final BooleanQuery booleanQuery = new BooleanQuery();
            booleanQuery.add(query, Occur.MUST);
            booleanQuery.add(new TermQuery(new Term("_index", index)), Occur.MUST);
            final TopDocs search = indexSearcher.search(booleanQuery, limit);

            return buildSearchResults(search);
        } catch (final IOException | ParseException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Used for unit testing.
     *
     * @param connection
     */
    public void setConnection(Connection connection) {

        this.connection = connection;
    }
}
