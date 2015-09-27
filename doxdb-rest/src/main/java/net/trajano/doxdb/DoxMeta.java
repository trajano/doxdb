package net.trajano.doxdb;

import java.io.Serializable;
import java.io.StringReader;
import java.security.Principal;
import java.util.Calendar;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.DatatypeConverter;

import net.trajano.doxdb.internal.DoxPrincipal;

@Embeddable
public class DoxMeta implements
    Serializable {

    /**
     * bare_field_name.
     */
    private static final long serialVersionUID = -910687815159740508L;

    private byte[] accessKey;

    private int collectionSchemaVersion;

    /**
     * Content in JSON format.
     */
    private String contentJson;

    private Principal createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    private DoxID doxId;

    private long id;

    private Principal lastUpdatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedOn;

    private int version;

    public DoxMeta() {

    }

    public DoxMeta(final long id,
        final String doxid,
        final int version,
        final int collectionSchemaVersion,
        final byte[] accessKey,
        final String createdBy,
        final Date createdOn,
        final String lastUpdatedBy,
        final Date lastUpdatedOn) {
        this.id = id;
        doxId = new DoxID(doxid);
        this.version = version;
        this.collectionSchemaVersion = collectionSchemaVersion;
        this.accessKey = accessKey;
        this.createdBy = new DoxPrincipal(createdBy);
        this.createdOn = createdOn;
        this.lastUpdatedBy = new DoxPrincipal(lastUpdatedBy);
        this.lastUpdatedOn = lastUpdatedOn;
    }

    public byte[] getAccessKey() {

        return accessKey;
    }

    public int getCollectionSchemaVersion() {

        return collectionSchemaVersion;
    }

    /**
     * Content as a JSON object.
     *
     * @return
     */
    public JsonObject getContent() {

        return Json.createReader(new StringReader(contentJson)).readObject();
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

    public int getVersion() {

        return version;
    }

    public void incrementVersion() {

        ++version;
    }

    public void setAccessKey(final byte[] accessKey) {

        this.accessKey = accessKey;
    }

    public void setCollectionSchemaVersion(final int collectionSchemaVersion) {

        this.collectionSchemaVersion = collectionSchemaVersion;
    }

    /**
     * Sets the content JSON with the DoxID and optimistic locking version data
     * decorated in.
     *
     * @param content
     *            content
     * @param doxId
     *            Dox ID
     * @param version
     *            optimistic locking version
     */
    public void setContentJson(final JsonObject content,
        final DoxID doxId,
        final int version) {

        final JsonObjectBuilder b = Json.createObjectBuilder();
        b.add("_id", doxId.toString());
        b.add("_version", version);
        for (final String key : content.keySet()) {
            if (!key.startsWith("_")) {
                b.add(key, content.get(key));
            }
        }
        contentJson = b.build().toString();
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

    public void setVersion(final int version) {

        this.version = version;
    }
}
