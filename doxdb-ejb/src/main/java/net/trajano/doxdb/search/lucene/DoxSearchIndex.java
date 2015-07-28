package net.trajano.doxdb.search.lucene;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;

import net.trajano.doxdb.ejb.internal.DoxLength;

@Entity
public class DoxSearchIndex {

    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(length = DoxLength.INDEX_FILE_LENGTH,
        nullable = false)
    private byte[] content;

    @Column(nullable = false)
    private int contentlength;

    @EmbeddedId
    private IndexFile indexFile;

    public byte[] getContent() {

        return content;
    }

    public int getContentlength() {

        return contentlength;
    }

    public IndexFile getIndexFile() {

        return indexFile;
    }

    public void setContent(final byte[] content) {

        this.content = content;
    }

    public void setContentlength(final int contentlength) {

        this.contentlength = contentlength;
    }

    public void setIndexFile(final IndexFile indexFile) {

        this.indexFile = indexFile;
    }

}
