package net.trajano.doxdb;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This represents a view of the data that will be stored in the index.
 *
 * @author Archimedes
 */
public class IndexView implements
    Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -378816417510576056L;

    private String collection;

    private final ConcurrentMap<String, Double> doubles = new ConcurrentHashMap<>();

    /**
     * Associated DoxID that was used to populate the view.
     */
    private DoxID doxID;

    /**
     * Defines the index that will store this data view. The purpose is to allow
     * multiple indices to co-exist and prevent accidental views from another
     * index. Underneath it all it is still one table. The value will be stored
     * in "_index" field.
     */
    private String index;

    private final ConcurrentMap<String, Long> longs = new ConcurrentHashMap<>();

    /**
     * Flag to indicate that the data should be masked. If the data is masked,
     * the ID and URLs should not appear in the search results.
     */
    private boolean masked = false;

    /**
     * If this is true, the entry will be removed from the index.
     */
    private boolean remove;

    private final ConcurrentMap<String, String> strings = new ConcurrentHashMap<>();

    private final StringBuilder text = new StringBuilder();

    private final ConcurrentMap<String, String> texts = new ConcurrentHashMap<>();

    /**
     * Appends the contents of the specified text to the full text search
     * content.
     *
     * @param text
     * @return
     */
    public IndexView appendText(final String text) {

        this.text.append(text)
            .append('\n');
        return this;
    }

    public String getCollection() {

        return collection;
    }

    public Set<Entry<String, Double>> getDoubles() {

        return doubles.entrySet();
    }

    public DoxID getDoxID() {

        return doxID;
    }

    public String getIndex() {

        return index;
    }

    public Set<Entry<String, Long>> getLongs() {

        return longs.entrySet();
    }

    public Set<Entry<String, String>> getStrings() {

        return strings.entrySet();
    }

    /**
     * The value will be stored in "_text" which will also be the default.
     *
     * @return
     */
    public String getText() {

        return text.toString();
    }

    public Set<Entry<String, String>> getTexts() {

        return texts.entrySet();
    }

    public boolean isMasked() {

        return masked;
    }

    /**
     * Gets remove.
     *
     * @return the remove
     */
    public boolean isRemove() {

        return remove;
    }

    public void setCollection(final String collection) {

        this.collection = collection;
    }

    public IndexView setDouble(final String name,
        final double value) {

        doubles.put(name, value);
        return this;
    }

    public void setDoxID(final DoxID doxId) {

        doxID = doxId;

    }

    public IndexView setIndex(final String index) {

        this.index = index;
        return this;
    }

    public IndexView setLong(final String name,
        final long value) {

        longs.put(name, value);
        return this;
    }

    public void setMasked(final boolean masked) {

        this.masked = masked;
    }

    /**
     * Sets remove.
     *
     * @param remove
     *            the remove to set
     */
    public void setRemove(final boolean remove) {

        this.remove = remove;
    }

    /**
     * Strings are not broken up into tokens and must be exact match. The
     * original text is stored in the index.
     *
     * @param name
     * @param stringValue
     * @return
     */
    public IndexView setString(final String name,
        final String stringValue) {

        strings.put(name, stringValue);
        return this;

    }

    /**
     * Text are broken up into tokens. The original text is not stored in the
     * index.
     *
     * @param name
     * @param stringValue
     * @return
     */
    public IndexView setText(final String name,
        final String stringValue) {

        texts.put(name, stringValue);
        return this;

    }
}
