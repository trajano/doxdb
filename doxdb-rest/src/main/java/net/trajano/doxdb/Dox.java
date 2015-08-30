package net.trajano.doxdb;

import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.LockModeType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;

import net.trajano.doxdb.ejb.internal.DoxLength;
import net.trajano.doxdb.internal.DoxPrincipal;

@Entity
@Table(
    indexes = @Index(columnList = "schemaName") ,
    uniqueConstraints = @UniqueConstraint(columnNames = {
        "doxId",
        "schemaName"
}) )
@NamedQueries({
    @NamedQuery(name = Dox.READ_META_BY_SCHEMA_NAME_DOX_ID,
        query = "select new net.trajano.doxdb.DoxMeta(e.id, e.doxId, e.version, e.schemaVersion, e.accessKey, e.createdBy, e.createdOn, e.lastUpdatedBy, e.lastUpdatedOn) from Dox e where e.schemaName = :schemaName and e.doxId = :doxId",
        lockMode = LockModeType.OPTIMISTIC),

    @NamedQuery(name = Dox.READ_FOR_UPDATE_META_BY_SCHEMA_NAME_DOX_ID_VERSION,
        query = "select new net.trajano.doxdb.DoxMeta(e.id, e.doxId, e.version, e.schemaVersion, e.accessKey, e.createdBy, e.createdOn, e.lastUpdatedBy, e.lastUpdatedOn) from Dox e where e.schemaName = :schemaName and e.doxId = :doxId and e.version = :version",
        lockMode = LockModeType.OPTIMISTIC_FORCE_INCREMENT),

    @NamedQuery(name = Dox.READ_ALL_BY_SCHEMA_NAME,
        query = "from Dox e where e.schemaName = :schemaName",
        lockMode = LockModeType.NONE)
})
public class Dox {

    /**
     * Named query {@value #READ_ALL_BY_SCHEMA_NAME};
     */
    public static final String READ_ALL_BY_SCHEMA_NAME = "readAllBySchemaName";

    /**
     * Named query {@value #READ_FOR_UPDATE_META_BY_SCHEMA_NAME_DOX_ID_VERSION};
     */
    public static final String READ_FOR_UPDATE_META_BY_SCHEMA_NAME_DOX_ID_VERSION = "readForUpdateMetaBySchemaNameDoxIDVersion";

    /**
     * Named query {@value #READ_META_BY_SCHEMA_NAME_DOX_ID};
     */
    public static final String READ_META_BY_SCHEMA_NAME_DOX_ID = "readMetaBySchemaNameDoxID";

    @Basic(fetch = FetchType.EAGER)
    @Column(nullable = true,
        length = DoxLength.ACCESS_KEY_LENGTH)
    private byte[] accessKey;

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

    @Column(nullable = false,
        columnDefinition = "CHAR(32)",
        length = DoxID.LENGTH)
    private String doxId;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false,
        length = DoxLength.PRINCIPAL_LENGTH)
    private String lastUpdatedBy;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedOn;

    @OneToMany(mappedBy = "parentDox",
        targetEntity = DoxOob.class,
        orphanRemoval = true,
        fetch = FetchType.LAZY)
    private Collection<DoxOob> oobs;

    @Column(nullable = false,
        updatable = false,
        length = DoxLength.SCHEMA_NAME_LENGTH)
    private String schemaName;

    @Column(nullable = false)
    private int schemaVersion;

    @Version
    private int version;

    /**
     * Creates the tombstone entity instance for the record.
     *
     * @param deletedBy
     *            user who deleted the record
     * @param deletedOn
     *            when it was deleted
     * @return tombstone data
     */
    public DoxTombstone buildTombstone(final Principal deletedBy,
        final Date deletedOn) {

        final DoxTombstone tombstone = new DoxTombstone();
        tombstone.setContent(content);
        tombstone.setCreatedBy(createdBy);
        tombstone.setCreatedOn(createdOn);
        tombstone.setDeletedBy(deletedBy.getName());
        tombstone.setDeletedOn(deletedOn);
        tombstone.setDoxId(getDoxId());
        tombstone.setLastUpdatedBy(lastUpdatedBy);
        tombstone.setLastUpdatedOn(lastUpdatedOn);
        tombstone.setSchemaName(schemaName);
        tombstone.setSchemaVersion(schemaVersion);
        return tombstone;
    }

    public byte[] getAccessKey() {

        return accessKey;
    }

    public BsonDocument getContent() {

        final BsonDocument decoded = new BsonDocumentCodec().decode(new BsonBinaryReader(ByteBuffer.wrap(content)), DecoderContext.builder()
            .build());
        decoded.put("_id", new BsonString(doxId.toString()));
        decoded.put("_version", new BsonInt32(version));
        return decoded;
    }

    public Principal getCreatedBy() {

        return new DoxPrincipal(createdBy);
    }

    public Date getCreatedOn() {

        return createdOn;
    }

    public DoxID getDoxId() {

        return new DoxID(doxId);
    }

    public long getId() {

        return id;
    }

    public String getJsonContent() {

        final BsonDocument decoded = new BsonDocumentCodec().decode(new BsonBinaryReader(ByteBuffer.wrap(content)), DecoderContext.builder()
            .build());
        decoded.remove("_id");
        decoded.remove("_version");
        return decoded.toJson();
    }

    public Principal getLastUpdatedBy() {

        return new DoxPrincipal(lastUpdatedBy);
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

    public int getVersion() {

        return version;
    }

    public void setAccessKey(final byte[] accessKey) {

        this.accessKey = accessKey;
    }

    public void setContent(final BsonDocument bson) {

        final BasicOutputBuffer basicOutputBuffer = new BasicOutputBuffer();
        new BsonDocumentCodec().encode(new BsonBinaryWriter(basicOutputBuffer), bson, EncoderContext.builder()
            .build());
        content = basicOutputBuffer.toByteArray();
    }

    public void setCreatedBy(final Principal createdBy) {

        this.createdBy = createdBy.getName();

    }

    public void setCreatedOn(final Date createdOn) {

        this.createdOn = createdOn;
    }

    /**
     * Sets the Dox ID value using a {@link DoxID}. This internally converts it
     * to a string as a workaround when JPA converters are not working as
     * expected.
     *
     * @param doxId
     *            Dox ID
     */
    public void setDoxId(final DoxID doxId) {

        this.doxId = doxId.toString();
    }

    public void setId(final long id) {

        this.id = id;
    }

    public void setLastUpdatedBy(final Principal lastUpdatedBy) {

        this.lastUpdatedBy = lastUpdatedBy.getName();

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

    public void setVersion(final int version) {

        this.version = version;
    }
}
