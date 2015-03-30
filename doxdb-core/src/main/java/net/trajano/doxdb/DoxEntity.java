package net.trajano.doxdb;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.xml.validation.Schema;

@MappedSuperclass
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "doxId"))
public abstract class DoxEntity {

    /**
     * Content.
     */
    @Lob
    @Column(nullable = false)
    @Basic(fetch = FetchType.LAZY)
    private byte[] content;

    @Column(nullable = false, length = DoxPrincipal.LENGTH, updatable = false)
    private DoxPrincipal createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdOn;

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private long id;

    @Column(nullable = false, length = DoxPrincipal.LENGTH)
    private DoxPrincipal lastUpdatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date lastUpdatedOn;

    @Column(nullable = false, length = DoxID.LENGTH, updatable = false)
    private DoxID doxId;

    @Version
    private int version;

    public byte[] getContent() {

        return content;
    }

    public DoxPrincipal getCreatedBy() {

        return createdBy;
    }

    public Date getCreatedOn() {

        return createdOn;
    }

    public DoxPrincipal getLastUpdatedBy() {

        return lastUpdatedBy;
    }

    public Date getLastUpdatedOn() {

        return lastUpdatedOn;
    }

    /**
     * Gets the schema for validating the data.
     *
     * @return
     */
    public Schema getSchema() {

        return null;
    }

    public DoxID getDoxID() {

        return doxId;
    }

    public void setContent(final byte[] content) {

        this.content = content;
    }

    public void setCreatedBy(final DoxPrincipal createdBy) {

        this.createdBy = createdBy;
    }

    public void setCreatedOn(final Date createdOn) {

        this.createdOn = createdOn;
    }

    public void setLastUpdatedBy(final DoxPrincipal lastUpdatedBy) {

        this.lastUpdatedBy = lastUpdatedBy;
    }

    public void setLastUpdatedOn(final Date lastUpdatedOn) {

        this.lastUpdatedOn = lastUpdatedOn;
    }

    public void setDoxID(final DoxID doxId) {

        this.doxId = doxId;
    }

}
