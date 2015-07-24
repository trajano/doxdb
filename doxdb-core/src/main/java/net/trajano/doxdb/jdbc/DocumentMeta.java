package net.trajano.doxdb.jdbc;

import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import net.trajano.doxdb.DoxID;

public class DocumentMeta {

    private byte[] accessKey;

    private int contentVersion;

    private DoxPrincipal createdBy;

    private Date createdOn;

    private DoxID doxId;

    private long id;

    private DoxPrincipal lastUpdatedBy;

    private Date lastUpdatedOn;

    private int version;

    public byte[] getAccessKey() {

        return accessKey;
    }

    public int getContentVersion() {

        return contentVersion;
    }

    public DoxPrincipal getCreatedBy() {

        return createdBy;
    }

    public Date getCreatedOn() {

        return createdOn;
    }

    public String getCreatedOnString() {

        final Calendar cal = Calendar.getInstance();
        cal.setTime(createdOn);
        return DatatypeConverter.printDateTime(cal);
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

    public String getLastUpdatedOnString() {

        final Calendar cal = Calendar.getInstance();
        cal.setTime(lastUpdatedOn);
        return DatatypeConverter.printDateTime(cal);
    }

    public int getVersion() {

        return version;
    }

    public void setAccessKey(final byte[] accessKey) {

        this.accessKey = accessKey;
    }

    public void setContentVersion(final int contentVersion) {

        this.contentVersion = contentVersion;
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
