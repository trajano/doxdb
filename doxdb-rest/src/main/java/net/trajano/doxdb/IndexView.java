package net.trajano.doxdb;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This represents a view of the data that will be stored in the index.
 *
 * @author Archimedes Trajano
 */
public class IndexView implements
    Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -378816417510576056L;

    private String collection;

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

    /**
     * Flag to indicate that the data should be masked. If the data is masked,
     * the ID and URLs should not appear in the search results.
     */
    private boolean masked = false;

    /**
     * Holds numeric data.
     */
    private final Map<String, BigDecimal> numbers = new HashMap<>();

    private final Map<String, String> strings = new HashMap<>();

    private final StringBuilder text = new StringBuilder();

    private final Map<String, String> texts = new HashMap<>();

    public IndexView() {

    }

    /**
     * Appends the contents of the specified text to the full text search
     * content.
     *
     * @param text
     *            text
     * @return <code>this</code>
     */
    public IndexView appendText(final String text) {

        this.text.append(text)
            .append('\n');
        return this;
    }

    public String getCollection() {

        return collection;
    }

    public DoxID getDoxID() {

        return doxID;
    }

    public String getIndex() {

        return index;
    }

    public Set<Entry<String, BigDecimal>> getNumbers() {

        return numbers.entrySet();
    }

    public Set<Entry<String, String>> getStrings() {

        return strings.entrySet();
    }

    /**
     * The value will be stored in "_text" which will also be the default.
     *
     * @return value of text
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

    public void setCollection(final String collection) {

        this.collection = collection;
    }

    public void setDoxID(final DoxID doxId) {

        doxID = doxId;

    }

    public IndexView setIndex(final String index) {

        this.index = index;
        return this;
    }

    public void setMasked(final boolean masked) {

        this.masked = masked;
    }

    /**
     * Sets a number attribute.
     *
     * @param name
     *            name of the attribute field
     * @param value
     *            numeric value.
     * @return <code>this</code>
     */
    public IndexView setNumber(final String name,
        final BigDecimal value) {

        numbers.put(name, value);
        return this;
    }

    /**
     * Sets a number attribute. Convenience to use <code>double</code>.
     *
     * @param name
     *            name of the attribute field
     * @param value
     *            numeric value.
     * @return <code>this</code>
     */
    public IndexView setNumber(final String name,
        final double value) {

        numbers.put(name, new BigDecimal(value));
        return this;
    }

    /**
     * Sets a number attribute. Convenience to use <code>long</code>.
     *
     * @param name
     *            name of the attribute field
     * @param value
     *            numeric value.
     * @return <code>this</code>
     */
    public IndexView setNumber(final String name,
        final long value) {

        numbers.put(name, new BigDecimal(value));
        return this;
    }

    /**
     * Strings are not broken up into tokens and must be exact match. The
     * original text is stored in the index.
     *
     * @param name
     *            field name
     * @param stringValue
     *            string value
     * @return <code>this</code>
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
     *            field name
     * @param textValue
     *            text value
     * @return <code>this</code>
     */
    public IndexView setText(final String name,
        final String textValue) {

        texts.put(name, textValue);
        return this;

    }
}
