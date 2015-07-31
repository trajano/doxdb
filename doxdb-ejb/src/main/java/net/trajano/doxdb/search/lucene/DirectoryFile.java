package net.trajano.doxdb.search.lucene;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import net.trajano.doxdb.ejb.internal.DoxLength;

@Embeddable
public class DirectoryFile implements
    Serializable {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -1263948702787964106L;

    @Column(nullable = false,
        length = DoxLength.INDEX_NAME_LENGTH)
    private String directoryName;

    @Column(nullable = false,
        length = DoxLength.INDEX_FILE_NAME_LENGTH)
    private String fileName;

    public DirectoryFile() {
    }

    public DirectoryFile(final String directoryName,
        final String fileName) {
        this.directoryName = directoryName;
        this.fileName = fileName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DirectoryFile other = (DirectoryFile) obj;
        if (directoryName == null) {
            if (other.directoryName != null) {
                return false;
            }
        } else if (!directoryName.equals(other.directoryName)) {
            return false;
        }
        if (fileName == null) {
            if (other.fileName != null) {
                return false;
            }
        } else if (!fileName.equals(other.fileName)) {
            return false;
        }
        return true;
    }

    public String getDirectoryName() {

        return directoryName;
    }

    public String getFileName() {

        return fileName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (directoryName == null ? 0 : directoryName.hashCode());
        result = prime * result + (fileName == null ? 0 : fileName.hashCode());
        return result;
    }

    public void setDirectoryName(final String directoryName) {

        this.directoryName = directoryName;
    }

    public void setFileName(final String fileName) {

        this.fileName = fileName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return "DirectoryFile [directoryName=" + directoryName + ", fileName=" + fileName + "]";
    }
}
