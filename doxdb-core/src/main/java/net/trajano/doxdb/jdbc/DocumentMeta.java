package net.trajano.doxdb.jdbc;

import java.util.Date;

import net.trajano.doxdb.DoxID;

public class DocumentMeta {

    private DoxPrincipal createdBy;

    private Date createdOn;

    private DoxID doxId;

    private long id;

    private DoxPrincipal lastUpdatedBy;

    private Date lastUpdatedOn;

    private int version;

    public DoxPrincipal getCreatedBy() {

        return createdBy;
    }

    public Date getCreatedOn() {

        return createdOn;
    }

    public DoxID getDoxId() {

        return doxId;
    }

    public long getId() {

        return id;
    }

    public DoxPrincipal getLastUpdatedBy() {

        return lastUpdatedBy;
    }

    public Date getLastUpdatedOn() {

        return lastUpdatedOn;
    }

    public int getVersion() {

        return version;
    }

    public void setCreatedBy(final DoxPrincipal createdBy) {

        this.createdBy = createdBy;
    }

    public void setCreatedOn(final Date createdOn) {

        this.createdOn = createdOn;
    }

    public void setDoxId(final DoxID doxId) {

        this.doxId = doxId;
    }

    public void setId(final long id) {

        this.id = id;
    }

    public void setLastUpdatedBy(final DoxPrincipal lastUpdatedBy) {

        this.lastUpdatedBy = lastUpdatedBy;
    }

    public void setLastUpdatedOn(final Date lastUpdatedOn) {

        this.lastUpdatedOn = lastUpdatedOn;
    }

    public void setVersion(final int version) {

        this.version = version;
    }

}
