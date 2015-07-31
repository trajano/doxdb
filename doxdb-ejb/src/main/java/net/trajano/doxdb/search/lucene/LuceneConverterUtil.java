package net.trajano.doxdb.search.lucene;

import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.IndexView;

/**
 * type.
 *
 * @author Archimedes Trajano
 */
public final class LuceneConverterUtil {

    public static final String FIELD_COLLECTION = "\t collection";

    public static final String FIELD_ID = "\t id";

    public static final String FIELD_INDEX = "\t index";

    public static final String FIELD_TEXT = "\t text";

    public static final String FIELD_UNIQUE_ID = "\t uid";

    public static IndexView buildFromDoc(final Document doc) {

        final IndexView ret = new IndexView();
        for (final IndexableField field : doc.getFields()) {
            if (FIELD_ID.equals(field.name()) || FIELD_UNIQUE_ID.equals(field.name()) || FIELD_COLLECTION.equals(field.name())) {
                continue;
            }
            final Number numericValue = field.numericValue();
            if (numericValue == null) {
                ret.setString(field.name(), field.stringValue());
            } else if (numericValue instanceof Double) {
                ret.setDouble(field.name(), numericValue
                    .doubleValue());
            } else if (numericValue instanceof Long) {
                ret.setLong(field.name(), numericValue
                    .longValue());
            }
        }
        final String idValue = doc.get(FIELD_ID);
        if (idValue != null) {
            ret.setDoxID(new DoxID(idValue));
        } else {
            ret.setMasked(true);
        }
        ret.setCollection(doc.get(FIELD_COLLECTION));
        return ret;

    }

    public static Document buildFromIndexView(
        final IndexView indexView) {

        final Document doc = new Document();
        doc.add(new StringField(FIELD_UNIQUE_ID, uid(indexView), Store.NO));
        doc.add(new StringField(FIELD_INDEX, indexView.getIndex(), Store.NO));
        doc.add(new StringField(FIELD_COLLECTION, indexView.getCollection(), Store.YES));
        if (!indexView.isMasked()) {
            doc.add(new StringField(FIELD_ID, indexView.getDoxID().toString(), Store.YES));
        } else {
            doc.add(new StringField(FIELD_ID, indexView.getDoxID().toString(), Store.NO));
        }
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

    /**
     * Create a unique ID for the search index record.
     *
     * @param view
     * @return
     */
    public static String uid(final IndexView view) {

        return view.getIndex() + "\t" + view.getCollection() + "\t" + view.getDoxID();
    }

    /**
     * Constructs LuceneConverterUtil.
     */
    private LuceneConverterUtil() {
    }
}
