package net.trajano.doxdb.search.lucene;

import static net.trajano.doxdb.search.lucene.LuceneConverterUtil.FIELD_COLLECTION;
import static net.trajano.doxdb.search.lucene.LuceneConverterUtil.FIELD_ID;
import static net.trajano.doxdb.search.lucene.LuceneConverterUtil.FIELD_UNIQUE_ID;
import static net.trajano.doxdb.search.lucene.LuceneConverterUtil.buildFromIndexView;
import static net.trajano.doxdb.search.lucene.LuceneConverterUtil.uid;

import java.io.IOException;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.persistence.PersistenceException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;

import net.trajano.doxdb.IndexView;

/**
 * type.
 *
 * @author Archimedes Trajano
 */
@MessageDriven(mappedName = "indexupdate",
    activationConfig = @ActivationConfigProperty(propertyName = "destination",
        propertyValue = "java:/queue/indexupdate") )
public class IndexUpdater implements
    MessageListener {

    @EJB
    LuceneDirectoryProvider directoryProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(final Message m) {

        try (final Directory dir = directoryProvider.provideDirectory()) {
            final Analyzer analyzer = new StandardAnalyzer();
            final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setWriteLockTimeout(5000);

            try (final IndexWriter indexWriter = new IndexWriter(dir, iwc)) {
                final IndexView[] indexViews = m.getBody(IndexView[].class);
                for (final IndexView indexView : indexViews) {
                    if (!indexView.isRemove()) {
                        final Document doc = buildFromIndexView(indexView);
                        indexWriter.updateDocument(new Term(FIELD_UNIQUE_ID, uid(indexView)), doc);
                    } else {
                        final BooleanQuery booleanQuery = new BooleanQuery();
                        booleanQuery.add(new TermQuery(new Term(FIELD_ID, indexView.getDoxID().toString())), Occur.MUST);
                        booleanQuery.add(new TermQuery(new Term(FIELD_COLLECTION, indexView.getCollection())), Occur.MUST);
                        indexWriter.deleteDocuments(booleanQuery);
                    }
                }
            }
        } catch (final JMSException
            | IOException e) {
            throw new PersistenceException(e);
        }
    }

}
