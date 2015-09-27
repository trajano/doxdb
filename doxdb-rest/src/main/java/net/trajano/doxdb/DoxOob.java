package net.trajano.doxdb;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import net.trajano.doxdb.ejb.internal.DoxLength;

/**
 * Out of band data for Dox.
 *
 * @author Archimedes Trajano
 */
@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {
        "parentId",
        "oobName"
}) )
public class DoxOob {

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false,
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

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false,
        length = DoxLength.PRINCIPAL_LENGTH)
    private String lastUpdatedBy;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedOn;

    @Column(nullable = false,
        updatable = false,
        length = DoxLength.OOB_NAME_LENGTH)
    private String oobName;

    @ManyToOne(fetch = FetchType.LAZY,
        optional = false)
    @JoinColumn(name = "parentId",
        nullable = false)
    private Dox parentDox;

    public byte[] getContent() {

        return content;
    }

    public String getCreatedBy() {

        return createdBy;
    }

    public Date getCreatedOn() {

        return createdOn;
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

    public String getOobName() {

        return oobName;
    }

    public Dox getParentDox() {

        return parentDox;
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

    public void setId(final long id) {

        this.id = id;
    }

    public void setLastUpdatedBy(final String lastUpdatedBy) {

        this.lastUpdatedBy = lastUpdatedBy;
    }

    public void setLastUpdatedOn(final Date lastUpdatedOn) {

        this.lastUpdatedOn = lastUpdatedOn;
    }

    public void setOobName(final String oobName) {

        this.oobName = oobName;
    }

    public void setParentDox(final Dox parentDox) {

        this.parentDox = parentDox;
    }

}
