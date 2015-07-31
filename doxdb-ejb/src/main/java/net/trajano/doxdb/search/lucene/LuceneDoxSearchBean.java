package net.trajano.doxdb.search.lucene;

import static net.trajano.doxdb.search.lucene.LuceneConverterUtil.FIELD_INDEX;
import static net.trajano.doxdb.search.lucene.LuceneConverterUtil.FIELD_TEXT;
import static net.trajano.doxdb.search.lucene.LuceneConverterUtil.buildFromDoc;

import java.io.IOException;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
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
import org.apache.lucene.store.Directory;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.IndexView;
import net.trajano.doxdb.SearchResult;
import net.trajano.doxdb.ejb.internal.DoxSearch;

/**
 * Handles lucene searches.
 *
 * @author Archimedes
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class LuceneDoxSearchBean implements
    DoxSearch {

    public static final String DIRECTORY_NAME = "DOX";

    @EJB
    LuceneDirectoryProvider directoryProvider;

    @SuppressWarnings("unused")
    private EntityManager em;

    @Inject
    private JMSContext jmsContext;

    @Resource(lookup = "java:/queue/indexupdate")
    private Queue queue;

    @Override
    public void addToIndex(
        final IndexView... indexViews) {

        jmsContext.createProducer().send(queue, indexViews);
    }

    private SearchResult buildSearchResults(final IndexSearcher indexSearcher,
        final TopDocs search) throws IOException {

        final SearchResult result = new SearchResult();
        result.setTotalHits(search.totalHits);
        for (final ScoreDoc scoreDoc : search.scoreDocs) {
            final Document doc = indexSearcher.doc(scoreDoc.doc);

            result.addHit(buildFromDoc(doc));
        }
        if (search.scoreDocs.length > 0) {
            result.setBottomDoc(search.scoreDocs[search.scoreDocs.length - 1].doc);
        }
        return result;
    }

    @Override
    public void removeFromIndex(final String collection,
        final DoxID doxID) {

        final IndexView[] removeView = new IndexView[1];
        removeView[0].setRemove(true);
        removeView[0].setCollection(collection);
        removeView[0].setDoxID(doxID);
        jmsContext.createProducer().send(queue, removeView);

    }

    /**
     * This will clear all the indexing data from the system.
     */
    @Override
    public void reset() {

        final Analyzer analyzer = new StandardAnalyzer();
        final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        final Directory dir = directoryProvider.provideDirectory();
        try (final IndexWriter indexWriter = new IndexWriter(dir, iwc)) {
            indexWriter.deleteAll();

        } catch (final IOException e) {
            throw new PersistenceException(e);
        }

    }

    @Override
    public SearchResult search(final String index,
        final String queryString,
        final int limit,
        final Integer fromDoc) {

        // TODO verify access to index for user
        try {
            final Analyzer analyzer = new StandardAnalyzer();
            final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            final QueryParser parser = new QueryParser(FIELD_TEXT, analyzer);
            final Query query = parser.parse(queryString);
            final BooleanQuery booleanQuery = new BooleanQuery();
            booleanQuery.add(query, Occur.MUST);
            booleanQuery.add(new TermQuery(new Term(FIELD_INDEX, index)), Occur.MUST);

            final Directory dir = directoryProvider.provideDirectory();

            try (final IndexWriter indexWriter = new IndexWriter(dir, iwc)) {

                final IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(indexWriter, true));
                final TopDocs search;
                if (fromDoc == null) {
                    search = indexSearcher.search(booleanQuery, limit);
                } else {
                    search = indexSearcher.searchAfter(new ScoreDoc(fromDoc, 0), query, limit);
                }
                return buildSearchResults(indexSearcher, search);
            }
        } catch (final IOException
            | ParseException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Injects the {@link EntityManager}.
     *
     * @param em
     *            entity manager
     */
    @PersistenceContext
    public void setEntityManager(final EntityManager em) {

        this.em = em;
    }

}
