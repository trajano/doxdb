package net.trajano.doxdb.search.lucene;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class DoxSearchIndexLock {

    @EmbeddedId
    private IndexFile indexFile;

    @Column(nullable = false)
    private boolean locked;

    public IndexFile getIndexFile() {

        return indexFile;
    }

    public boolean isLocked() {

        return locked;
    }

    public void setIndexFile(final IndexFile indexFile) {

        this.indexFile = indexFile;
    }

    public void setLocked(final boolean locked) {

        this.locked = locked;
    }

}
