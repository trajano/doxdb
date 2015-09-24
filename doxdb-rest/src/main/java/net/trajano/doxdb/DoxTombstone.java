package net.trajano.doxdb;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import net.trajano.doxdb.ejb.internal.DoxLength;

@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {
        "doxId",
        "schemaName"
}) )
public class DoxTombstone {

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false,
        updatable = false,
        length = DoxLength.CONTENT_LENGTH)
    private byte[] content;

    @Column(nullable = false,
        updatable = false,
        length = DoxLength.PRINCIPAL_LENGTH)
    private String createdBy;

    @Column(nullable = false,
        updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Column(nullable = false,
        updatable = false,
        length = DoxLength.PRINCIPAL_LENGTH)
    private String deletedBy;

    @Column(nullable = false,
        updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletedOn;

    @Column(nullable = false,
        updatable = false,
        columnDefinition = "CHAR(32)",
        length = DoxID.LENGTH)
    private String doxId;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false,
        updatable = false,
        length = DoxLength.PRINCIPAL_LENGTH)
    private String lastUpdatedBy;

    @Column(nullable = false,
        updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedOn;

    @Column(nullable = false,
        updatable = false,
        length = DoxLength.COLLECTION_NAME_LENGTH)
    private String schemaName;

    @Column(nullable = false,
        updatable = false)
    private int schemaVersion;

    public byte[] getContent() {

        return content;
    }

    public String getCreatedBy() {

        return createdBy;
    }

    public Date getCreatedOn() {

        return createdOn;
    }

    public String getDeletedBy() {

        return deletedBy;
    }

    public Date getDeletedOn() {

        return deletedOn;
    }

    public DoxID getDoxId() {

        return new DoxID(doxId);
    }

    public long getId() {

        return id;
    }

    public String getLastUpdatedBy() {

        return lastUpdatedBy;
    }

    public Date getLastUpdatedOn() {

        return lastUpdatedOn;
    }

    public String getSchemaName() {

        return schemaName;
    }

    public int getSchemaVersion() {

        return schemaVersion;
    }

    public void setContent(final byte[] content) {

        this.content = content;
    }

    public void setCreatedBy(final String createdBy) {

        this.createdBy = createdBy;
    }

    public void setCreatedOn(final Date createdOn) {

        this.createdOn = createdOn;
    }

    public void setDeletedBy(final String deletedBy) {

        this.deletedBy = deletedBy;
    }

    public void setDeletedOn(final Date deletedOn) {

        this.deletedOn = deletedOn;
    }

    public void setDoxId(final DoxID doxId) {

        this.doxId = doxId.toString();
    }

    public void setId(final long id) {

        this.id = id;
    }

    public void setLastUpdatedBy(final String lastUpdatedBy) {

        this.lastUpdatedBy = lastUpdatedBy;
    }

    public void setLastUpdatedOn(final Date lastUpdatedOn) {

        this.lastUpdatedOn = lastUpdatedOn;
    }

    public void setSchemaName(final String schemaName) {

        this.schemaName = schemaName;
    }

    public void setSchemaVersion(final int schemaVersion) {

        this.schemaVersion = schemaVersion;
    }
}
