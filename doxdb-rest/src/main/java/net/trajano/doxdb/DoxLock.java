package net.trajano.doxdb;

import java.security.Principal;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.LockModeType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import net.trajano.doxdb.ejb.internal.DoxLength;
import net.trajano.doxdb.internal.DoxPrincipal;

@Entity
@Table(
    indexes = {
        @Index(columnList = "lockedDoxId,lockId")
},
    uniqueConstraints = @UniqueConstraint(columnNames = {
        "lockedDoxId"
}) )
@NamedQueries({
    @NamedQuery(name = DoxLock.READ_LOCK_BY_COLLECTION_NAME_DOX_ID,
        query = "from DoxLock e where e.lockedDox.collectionName = :collectionName and e.lockedDox.doxId = :doxId",
        lockMode = LockModeType.NONE),

    @NamedQuery(name = DoxLock.REMOVE_LOCK_BY_COLLECTION_NAME_DOX_ID_LOCK_ID,
        query = "delete from DoxLock e where e.lockedDox.collectionName = :collectionName and e.lockedDox.doxId = :doxId and e.lockId = :lockId"),

    @NamedQuery(name = DoxLock.READ_LOCK_BY_COLLECTION_NAME_DOX_ID_LOCK_ID,
        query = "from DoxLock e where e.lockedDox.collectionName = :collectionName and e.lockedDox.doxId = :doxId and e.lockId = :lockId",
        lockMode = LockModeType.NONE)

})
public class DoxLock {

    public static final String COLLECTION_NAME = "collectionName";

    public static final String DOXID = "doxId";

    public static final String LOCKID = "lockId";

    /**
     * Named query {@value #READ_LOCK_BY_COLLECTION_NAME_DOX_ID};
     */
    public static final String READ_LOCK_BY_COLLECTION_NAME_DOX_ID = "readLockByCollectionNameDoxID";

    /**
     * Named query {@value #READ_LOCK_BY_COLLECTION_NAME_DOX_ID_LOCK_ID};
     */
    public static final String READ_LOCK_BY_COLLECTION_NAME_DOX_ID_LOCK_ID = "readLockByCollectionNameDoxIDLockId";

    /**
     * Named query {@value #REMOVE_LOCK_BY_COLLECTION_NAME_DOX_ID_LOCK_ID};
     */
    public static final String REMOVE_LOCK_BY_COLLECTION_NAME_DOX_ID_LOCK_ID = "removeLockByCollectionNameDoxIDLockId";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    /**
     * Principal of the person that requested the lock.
     */
    @Column(nullable = false,
        updatable = false,
        length = DoxLength.PRINCIPAL_LENGTH)
    private String lockedBy;

    @ManyToOne(fetch = FetchType.LAZY,
        optional = false)
    @JoinColumn(name = "lockedDoxId",
        nullable = false)
    private Dox lockedDox;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lockedOn;

    @Column(nullable = false)
    private int lockId;

    /**
     * Generates a new lock ID. It does not use a cryptographically secure RNG,
     * but this is not used for cryptography.
     *
     * @return the new lock ID.
     */
    public int generateLockID() {

        lockId = ThreadLocalRandom.current().nextInt();
        return lockId;
    }

    public Principal getLockedBy() {

        return new DoxPrincipal(lockedBy);
    }

    public Dox getLockedDox() {

        return lockedDox;
    }

    public Date getLockedOn() {

        return lockedOn;
    }

    public int getLockId() {

        return lockId;
    }

    public void setLockedBy(final Principal lockedBy) {

        this.lockedBy = lockedBy.getName();

    }

    public void setLockedBy(final String lockedBy) {

        this.lockedBy = lockedBy;
    }

    public void setLockedDox(final Dox lockedDox) {

        this.lockedDox = lockedDox;
    }

    public void setLockedOn(final Date lastUpdatedOn) {

        lockedOn = lastUpdatedOn;
    }
}
