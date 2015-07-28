package net.trajano.doxdb.search.lucene;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import net.trajano.doxdb.ejb.internal.DoxLength;

@Embeddable
public class IndexFile implements
    Serializable {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 4160874430747594107L;

    @Column(length = DoxLength.INDEX_FILE_NAME_LENGTH)
    private String fileName;

    @Column(length = DoxLength.INDEX_NAME_LENGTH)
    private String indexName;

    public String getFileName() {

        return fileName;
    }

    public String getIndexName() {

        return indexName;
    }

    public void setFileName(final String fileName) {

        this.fileName = fileName;
    }

    public void setIndexName(final String indexName) {

        this.indexName = indexName;
    }

}
