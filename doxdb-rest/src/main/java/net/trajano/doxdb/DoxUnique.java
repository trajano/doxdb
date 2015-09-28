package net.trajano.doxdb;

import java.util.ArrayList;
import java.util.List;

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
import javax.persistence.UniqueConstraint;

import net.trajano.doxdb.ejb.internal.DoxLength;
import net.trajano.doxdb.jsonpath.JsonPath;
import net.trajano.doxdb.schema.LookupType;
import net.trajano.doxdb.schema.SchemaType;

/**
 * Unique lookup table. Provides a secondary lookup table for the Dox data. It
 * is used for simple lookups without resorting to using an elasticsearch index.
 * Unlike DoxLookup, the mapped data must be unique.
 *
 * @author Archimedes Trajano
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
    columnNames = {
        "collectionName",
        "lookupName",
        "lookupKey"
}) ,
    indexes = @Index(columnList = "doxId") )
@NamedQueries({
    @NamedQuery(name = DoxUnique.UNIQUE_LOOKUP,
        query = "select u.dox from DoxUnique u where u.collectionName = :collectionName and u.lookupName = :lookupName and u.lookupKey = :lookupKey",
        lockMode = LockModeType.NONE),
    @NamedQuery(name = DoxUnique.REMOVE_UNIQUE_FOR_DOX,
        query = "delete from DoxUnique u where u.dox = :dox"),
    @NamedQuery(name = DoxUnique.REMOVE_ALL,
        query = "delete from DoxUnique"),
    @NamedQuery(name = DoxUnique.UPDATE_UNIQUE_FOR_DOX,
        query = "update DoxUnique u set u.lookupKey = :lookupKey where u.dox = :dox")
})
public class DoxUnique {

    public static final String COLLECTION_NAME = "collectionName";

    public static final String LOOKUP_KEY = "lookupKey";

    public static final String LOOKUP_NAME = "lookupName";

    public static final String REMOVE_ALL = "removeAllUnique";

    /**
     * Named query {@value #REMOVE_UNIQUE_FOR_DOX}.
     */
    public static final String REMOVE_UNIQUE_FOR_DOX = "removeUniqueForDox";

    /**
     * Named query {@value #UNIQUE_LOOKUP}.
     */
    public static final String UNIQUE_LOOKUP = "uniqueLookup";

    /**
     * Named query {@value #UPDATE_UNIQUE_FOR_DOX}.
     */
    public static final String UPDATE_UNIQUE_FOR_DOX = "updateUniqueForDox";

    /**
     * Creates an list of {@link DoxUnique} based on the data from a {@link Dox}
     * and the {@link SchemaType}.
     *
     * @param dox
     *            dox
     * @param schemaType
     *            schema type
     * @return list of {@link DoxUnique}.
     */
    public static List<DoxUnique> fromDox(final Dox dox,
        final SchemaType schemaType) {

        final List<DoxUnique> a = new ArrayList<>(schemaType.getUnique().size());
        for (final LookupType lookup : schemaType.getUnique()) {
            final DoxUnique r = new DoxUnique();
            r.collectionName = dox.getCollectionName();
            r.dox = dox;
            r.lookupName = lookup.getName();
            r.lookupKey = JsonPath.compile(lookup.getPath()).read(dox.getJsonObject().toString());
            a.add(r);
        }

        return a;

    }

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
