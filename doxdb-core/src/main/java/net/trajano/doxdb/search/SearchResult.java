package net.trajano.doxdb.search;

import java.util.LinkedList;
import java.util.List;

public class SearchResult {

    private final List<IndexView> hits = new LinkedList<>();

    private int totalHits;

    public void addHit(IndexView hit) {

        hits.add(hit);
    }

    public IndexView[] getHits() {

        return hits.toArray(new IndexView[0]);
    }

    public int getTotalHits() {

        return totalHits;
    }

    public void setTotalHits(int totalHits) {

        this.totalHits = totalHits;
    }
}
