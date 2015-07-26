package net.trajano.doxdb;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SearchResult implements
    Serializable {

    /**
     * bare_field_name.
     */
    private static final long serialVersionUID = 4059685393021065762L;

    private final List<IndexView> hits = new LinkedList<>();

    private int totalHits;

    public void addHit(final IndexView hit) {

        hits.add(hit);
    }

    public List<IndexView> getHits() {

        return Collections.unmodifiableList(hits);
    }

    public int getTotalHits() {

        return totalHits;
    }

    public void setTotalHits(final int totalHits) {

        this.totalHits = totalHits;
    }
}
