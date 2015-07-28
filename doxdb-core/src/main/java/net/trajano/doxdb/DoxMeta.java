package net.trajano.doxdb;

import java.io.Serializable;
import java.security.Principal;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import net.trajano.doxdb.internal.DoxPrincipal;

public class DoxMeta implements
    Serializable {

    /**
     * bare_field_name.
     */
    private static final long serialVersionUID = -910687815159740508L;

    private byte[] accessKey;

    /**
     * Content in JSON format.
     */
    private String contentJson;

    private Principal createdBy;

    private Date createdOn;

    private DoxID doxId;

    private long id;

    private Principal lastUpdatedBy;

    private Date lastUpdatedOn;

    private int schemaVersion;

    private int version;

    public DoxMeta() {

    }

    public DoxMeta(final long id,
        final String doxid,
        final int version,
        final int schemaVersion,
        final byte[] accessKey,
        final String createdBy,
        final Date createdOn,
        final String lastUpdatedBy,
        final Date lastUpdatedOn) {
        this.id = id;
        doxId = new DoxID(doxid);
        this.version = version;
        this.schemaVersion = schemaVersion;
        this.accessKey = accessKey;
        this.createdBy = new DoxPrincipal(createdBy);
        this.createdOn = createdOn;
        this.lastUpdatedBy = new DoxPrincipal(lastUpdatedBy);
        this.lastUpdatedOn = lastUpdatedOn;
    }

    public byte[] getAccessKey() {

        return accessKey;
    }

    public String getContentJson() {

        return contentJson;
    }

    public Principal getCreatedBy() {

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

    public Principal getLastUpdatedBy() {

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

    public int getSchemaVersion() {

        return schemaVersion;
    }

    public int getVersion() {

        return version;
    }

    public void incrementVersion() {

        ++version;
    }

    public void setAccessKey(final byte[] accessKey) {

        this.accessKey = accessKey;
    }

    public void setContentJson(final String contentJson) {

        this.contentJson = contentJson;
    }

    public void setCreatedBy(final Principal createdBy) {

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

    public void setLastUpdatedBy(final Principal lastUpdatedBy) {

        this.lastUpdatedBy = lastUpdatedBy;
    }

    public void setLastUpdatedOn(final Date lastUpdatedOn) {

        this.lastUpdatedOn = lastUpdatedOn;
    }

    public void setSchemaVersion(final int contentVersion) {

        schemaVersion = contentVersion;
    }

    public void setVersion(final int version) {

        this.version = version;
    }
}
