package net.trajano.doxdb.search.lucene;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;

import net.trajano.doxdb.ejb.internal.DoxLength;

/**
 * Search directory data. In order to support Oracle, what would normally have
 * been a unique constraint against directory + filename was made non-unique.
 *
 * @author Archimedes Trajano
 */
@Entity
@Table(indexes = {
    @Index(columnList = "directoryName",
        unique = false),
    @Index(columnList = "directoryName,fileName",
        unique = false)

})
public class DoxSearchIndex {

    /**
     * Content. This is nullable in order to support Oracle.
     */
    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(length = DoxLength.INDEX_FILE_LENGTH,
        nullable = false)
    private byte[] content;

    @Column(nullable = false)
    private int contentLength;

    @Column(nullable = false,
        length = DoxLength.INDEX_NAME_LENGTH)
    private String directoryName;

    @Column(nullable = false,
        length = DoxLength.INDEX_FILE_NAME_LENGTH)
    private String fileName;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    public byte[] getContent() {

        return content;
    }

    public int getContentLength() {

        return contentLength;
    }

    /**
     * Gets directoryName.
     *
     * @return the directoryName
     */
    public String getDirectoryName() {

        return directoryName;
    }

    /**
     * Gets fileName.
     *
     * @return the fileName
     */
    public String getFileName() {

        return fileName;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public long getId() {

        return id;
    }

    public void setContent(final byte[] content) {

        this.content = content;
    }

    public void setContentLength(final int contentlength) {

        contentLength = contentlength;
    }

    /**
     * Sets directoryName.
     *
     * @param directoryName
     *            the directoryName to set
     */
    public void setDirectoryName(final String directoryName) {

        this.directoryName = directoryName;
    }

    /**
     * Sets fileName.
     *
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(final String fileName) {

        this.fileName = fileName;
    }

    /**
     * Sets id.
     *
     * @param id
     *            the id to set
     */
    public void setId(final long id) {

        this.id = id;
    }

}
