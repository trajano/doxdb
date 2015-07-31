package net.trajano.doxdb.search.lucene;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
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
        unique = false)

})
public class DoxSearchIndex {

    @Lob
    @Column(length = DoxLength.INDEX_FILE_LENGTH,
        nullable = false)
    private byte[] content;

    @Column(nullable = false)
    private int contentLength;

    @EmbeddedId
    private DirectoryFile directoryFile;

    public byte[] getContent() {

        return content;
    }

    public int getContentLength() {

        return contentLength;
    }

    public DirectoryFile getDirectoryFile() {

        return directoryFile;
    }

    public void setContent(final byte[] content) {

        this.content = content;
    }

    public void setContentLength(final int contentlength) {

        contentLength = contentlength;
    }

    public void setDirectoryFile(final DirectoryFile directoryFile) {

        this.directoryFile = directoryFile;
    }

}
