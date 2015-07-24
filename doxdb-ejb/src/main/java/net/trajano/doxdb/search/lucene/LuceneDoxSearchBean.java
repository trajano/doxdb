package net.trajano.doxdb.search.lucene;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
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

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.search.IndexView;
import net.trajano.doxdb.search.SearchResult;

/**
 * Implementers of this class do not require the {@link Singleton} annotation.
 *
 * @author Archimedes
 */
@Stateless
public class LuceneDoxSearchBean implements
    DoxSearch {

    private static final String FIELD_ID = "\t id";

    private static final String FIELD_INDEX = "\t index";

    private static final String FIELD_TEXT = "\t text";

    /**
     * The data source. It is required that the datasource be XA enabled so it
     * can co-exist with JPA and other operations.
     */
    @Resource
    private DataSource ds;

    private transient IndexWriterConfig iwc;

    @Override
    public void addToIndex(final String index,
        final DoxID doxid,
        final IndexView indexView) {

        try (final Connection c = ds.getConnection()) {
            final JdbcDirectory dir = new JdbcDirectory(c, "SEARCHINDEX");
            try (final IndexWriter indexWriter = new IndexWriter(dir, iwc)) {
                final Document doc = buildFromIndexView(index, doxid, indexView);
                indexWriter.updateDocument(new Term(FIELD_ID, doxid
                    .toString()), doc);
            }
        } catch (final IOException
            | SQLException e) {
            throw new PersistenceException(e);
        }
    }

    private IndexView buildFromDoc(final Document doc) {

        final IndexView ret = new IndexView();
        for (final IndexableField field : doc.getFields()) {
            if (FIELD_INDEX.equals(field.name())) {
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

    private Document buildFromIndexView(final String index,
        final DoxID doxid,
        final IndexView indexView) {

        final Document doc = new Document();
        doc.add(new StringField(FIELD_INDEX, index, Store.YES));
        doc.add(new StringField(FIELD_ID, doxid.toString(), Store.YES));
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
        doc.add(new TextField(FIELD_TEXT, indexView.getText(), Store.NO));
        return doc;

    }

    private SearchResult buildSearchResults(final IndexSearcher indexSearcher,
        final TopDocs search) throws IOException {

        final SearchResult result = new SearchResult();
        result.setTotalHits(search.totalHits);
        for (final ScoreDoc scoreDoc : search.scoreDocs) {
            final Document doc = indexSearcher.doc(scoreDoc.doc);

            result.addHit(buildFromDoc(doc));
        }
        return result;
    }

    @PostConstruct
    public void init() {

        final Analyzer analyzer = new StandardAnalyzer();
        iwc = new IndexWriterConfig(analyzer);
    }

    @Override
    public void removeFromIndex(final String index,
        final DoxID doxID) {

    }

    @Override
    public SearchResult search(final String index,
        final String queryString,
        final int limit) {

        try (final Connection c = ds.getConnection()) {
            final Analyzer analyzer = new StandardAnalyzer();

            final QueryParser parser = new QueryParser(FIELD_TEXT, analyzer);
            final Query query = parser.parse(queryString);
            final BooleanQuery booleanQuery = new BooleanQuery();
            booleanQuery.add(query, Occur.MUST);
            booleanQuery.add(new TermQuery(new Term(FIELD_INDEX, index)), Occur.MUST);
            final JdbcDirectory dir = new JdbcDirectory(c, "SEARCHINDEX");
            try (final IndexWriter indexWriter = new IndexWriter(dir, iwc)) {

                final IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(indexWriter, true));
                final TopDocs search = indexSearcher.search(booleanQuery, limit);

                return buildSearchResults(indexSearcher, search);
            }
        } catch (final IOException
            | ParseException
            | SQLException e) {
            throw new PersistenceException(e);
        }
    }

}
