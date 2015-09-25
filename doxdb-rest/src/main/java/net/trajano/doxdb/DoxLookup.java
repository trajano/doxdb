package net.trajano.doxdb;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.LockModeType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import net.trajano.doxdb.ejb.internal.DoxLength;

/**
 * Provides a secondary lookup table for the Dox data. It is used for simple
 * lookups without resorting to using an elasticsearch index.
 *
 * @author Archimedes Trajano
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
    columnNames = {
        "collectionName",
        "lookupName"
}) )
@NamedQueries({
    @NamedQuery(name = DoxLookup.LOOKUP,
        query = "select Dox d from DoxLookup u where u.collectionName = :collectionName and u.lookupName = :lookupName and u.lookupKey = :lookupKey",
        lockMode = LockModeType.PESSIMISTIC_READ),
})
public class DoxLookup {

    /**
     * Named query {@value #LOOKUP}.
     */
    public static final String LOOKUP = "lookup";

    @Column(nullable = false,
        insertable = true,
        updatable = false)
    private String collectionName;

    @ManyToOne(fetch = FetchType.LAZY,
        optional = false)
    @JoinColumn(name = "doxId",
        nullable = false)
    private Dox dox;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false,
        length = DoxLength.LOOKUP_KEY_LENGTH,
        insertable = true,
        updatable = false)
    private String lookupKey;

    @Column(nullable = false,
        length = DoxLength.LOOKUP_NAME_LENGTH,
        insertable = true,
        updatable = false)
    private String lookupName;

    public String getCollectionName() {

        return collectionName;
    }

    public Dox getDox() {

        return dox;
    }

    public long getId() {

        return id;
    }

    public String getLookupKey() {

        return lookupKey;
    }

    public String getLookupName() {

        return lookupName;
    }

    public void setCollectionName(final String collectionName) {

        this.collectionName = collectionName;
    }

    public void setDox(final Dox dox) {

        this.dox = dox;
    }

    public void setLookupKey(final String lookupKey) {

        this.lookupKey = lookupKey;
    }

    public void setLookupName(final String lookupName) {

        this.lookupName = lookupName;
    }

}
