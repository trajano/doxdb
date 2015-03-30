package net.trajano.doxdb;

import java.util.Date;

public class DocumentMeta {

    private DoxPrincipal createdBy;

    private Date createdOn;

    private long id;

    private DoxPrincipal lastUpdatedBy;

    private Date lastUpdatedOn;

    private DoxID doxId;

    private int version;

    public DoxPrincipal getCreatedBy() {

        return createdBy;
    }

    public void setCreatedBy(DoxPrincipal createdBy) {

        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {

        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {

        this.createdOn = createdOn;
    }

    public long getId() {

        return id;
    }

    public void setId(long id) {

        this.id = id;
    }

    public DoxPrincipal getLastUpdatedBy() {

        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(DoxPrincipal lastUpdatedBy) {

        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Date getLastUpdatedOn() {

        return lastUpdatedOn;
    }

    public void setLastUpdatedOn(Date lastUpdatedOn) {

        this.lastUpdatedOn = lastUpdatedOn;
    }

    public DoxID getDoxId() {

        return doxId;
    }

    public void setDoxId(DoxID doxId) {

        this.doxId = doxId;
    }

    public int getVersion() {

        return version;
    }

    public void setVersion(int version) {

        this.version = version;
    }

}
