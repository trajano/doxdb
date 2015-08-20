package net.trajano.doxdb.ejb;

import javax.ejb.Local;

import org.bson.BsonDocument;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxMeta;
import net.trajano.doxdb.SearchResult;

@Local
public interface DoxLocal {

    /**
     * Creates a dox record into the database. This will allocate a "_id" value
     * for the record.
     *
     * @param collectionName
     *            collection name
     * @param contents
     *            dox contents as a BSON. The contents MUST be valid for the
     *            schema.
     * @return dox meta with contents with "_id" and "_version" set.
     */
    DoxMeta create(String collectionName,
        BsonDocument contents);

    /**
     * @param collection
     * @param doxID
     * @param version
     * @return true if a record was deleted.
     */
    boolean delete(String collection,
        DoxID doxID,
        int version);

    /**
     * Does nothing, but calling it ensures that the EJB gets initialized.
     */
    void noop();

    DoxMeta read(String collectionName,
        DoxID id);

    /**
     * <p>
     * Builds a JSON array represented as a string containing the contents of
     * the collection. Each content entry is modified to have an "_id" and
     * "_version" attribute as well. This is meant for small collections as the
     * entire collection is loaded up into memory. Future releases may add a
     * largeReadAll method that will write to a temporary file that the client
     * would retrieve and delete later.
     * </p>
     * <p>
     * This is done to ensure the non-ASCII characters are sent correctly to the
     * clients, passing JsonArray or BSON appears to lose the non-ASCII
     * characters in transit.
     * </p>
     *
     * @param collectionName
     *            collection name
     * @return JSON string
     */
    String readAll(String collectionName);

    void reindex();

    SearchResult search(String index,
        String queryString,
        int i);

    SearchResult search(String index,
        String queryString,
        int limit,
        Integer fromDoc);

    /**
     * Creates a dox record into the database. This will allocate a "_id" value
     * for the record.
     *
     * @param collectionName
     *            collection name
     * @param contents
     *            dox contents as a BSON. The contents MUST be valid for the
     *            schema.
     * @return dox meta with contents with "_id" and "_version" set.
     */
    DoxMeta update(String collection,
        DoxID id,
        BsonDocument contents,
        int version);

}
