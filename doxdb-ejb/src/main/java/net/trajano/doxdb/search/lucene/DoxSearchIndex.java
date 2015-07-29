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
import javax.persistence.UniqueConstraint;

import net.trajano.doxdb.ejb.internal.DoxLength;

@Entity
@Table(indexes = @Index(columnList = "directoryName",
    unique = false) ,
    uniqueConstraints = @UniqueConstraint(columnNames = {
        "directoryName",
        "fileName"
}) )
public class DoxSearchIndex {

    /**
     * Content. This is nullable in order to support Oracle.
     */
    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(length = DoxLength.INDEX_FILE_LENGTH,
        nullable = true)
    private byte[] content;

    @Column(nullable = false)
    private int contentlength;

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

    public int getContentlength() {

        return contentlength;
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

    public void setContentlength(final int contentlength) {

        this.contentlength = contentlength;
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
