package net.trajano.doxdb.ejb;

import java.io.InputStream;

import javax.ejb.Local;
import javax.json.JsonArray;
import javax.json.JsonObject;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxMeta;
import net.trajano.doxdb.SearchResult;
import net.trajano.doxdb.schema.DoxPersistence;

@Local
public interface DoxLocal {

    SearchResult advancedSearch(String index,
        JsonObject query);

    SearchResult advancedSearch(String index,
        String schemaName,
        JsonObject query);

    /**
     * Creates a dox record into the database. This will allocate a "_id" value
     * for the record.
     *
     * @param collectionName
     *            collection name
     * @param content
     *            dox contents as a JSON. The contents MUST be valid for the
     *            schema.
     * @return dox meta with contents with "_id" and "_version" set.
     */
    DoxMeta create(String collectionName,
        JsonObject content);

    /**
     * @param collectionName
     *            collection name
     * @param doxID
     *            Dox ID
     * @param version
     *            version
     * @param extra
     *            extra data
     * @return true if a record was deleted.
     */
    boolean delete(String collectionName,
        DoxID doxID,
        int version,
        JsonObject extra);

    DoxPersistence getConfiguration();

    /**
     * This retrieves the schema.
     *
     * @param path
     *            path to the schema file
     * @return JSON Schema stream
     */
    InputStream getSchema(String path);

    boolean isLocked(String collectionName,
        DoxID doxId);

    /**
     * Locks a Dox.
     *
     * @param collectionName
     *            collection name
     * @param doxId
     *            Dox ID
     * @return lock ID as an integer
     */
    int lock(String collectionName,
        DoxID doxId);

    /**
     * Does nothing, but calling it ensures that the EJB gets initialized.
     */
    void noop();

    /**
     * Returns null if the record is not found.
     */
    DoxMeta read(String collectionName,
        DoxID id);

    /**
     * <p>
     * Builds a JSON array represented as a string containing the contents of
     * the collection. Each content entry is modified to have an "_id" and
     * "_version" attribute as well.
     * </p>
     * <p>
     * For small collections, "memory" can be used to transmit the entire
     * collection. In this mode, it will return a String containing the JSON
     * array and can be indicated by checking if the first character is "[".
     * </p>
     * <p>
     * For larger collection "file" should be used.In this mode, it will return
     * a String containing the name of the temporary file that can be opened
     * using a FileInputStream. The clients are expected to delete the file
     * afterwards.
     * </p>
     * <p>
     * This is done to ensure the non-ASCII characters are sent correctly to the
     * clients, passing JsonArray or BSON appears to lose the non-ASCII
     * characters in transit.
     * </p>
     *
     * @param schemaName
     *            schema name
     * @return JSON string or file name
     */
    String readAll(String schemaName);

    JsonArray readByLookup(String collectionName,
        String lookupName,
        String lookupKey);

    /**
     * Returns null if the record is not found.
     */
    DoxMeta readByUniqueLookup(String collectionName,
        String lookupName,
        String lookupKey);

    /**
     * Delete all the index data and reindex all the documents.
     */
    void reindex();

    SearchResult search(String index,
        String queryString,
        int i);

    SearchResult search(String index,
        String queryString,
        int limit,
        Integer fromDoc);

    SearchResult searchWithCollectionName(String index,
        String collectionName,
        String queryString,
        int limit,
        Integer fromDoc);

    /**
     * Unlocks a record.
     *
     * @param collectionName
     * @param doxId
     * @param lockId
     */
    void unlock(String collectionName,
        DoxID doxId,
        int lockId);

    /**
     * Updates a Dox record into the database. The collection must not support
     * locking for this to work.
     *
     * @param collectionName
     *            collection name
     * @param doxId
     *            DoxID
     * @param content
     *            dox contents as a JSON. The contents MUST be valid for the
     *            schema.
     * @param version
     *            optimistic locking version
     * @return dox meta with contents with "_id" and "_version" set.
     */
    DoxMeta update(String schemaName,
        DoxID id,
        JsonObject contents,
        int version);

    /**
     * Updates a locked Dox record into the database. The record must be locked
     * before the update can be performed.
     *
     * @param collectionName
     *            collection name
     * @param doxId
     *            DoxID
     * @param content
     *            dox contents as a JSON. The contents MUST be valid for the
     *            schema.
     * @param version
     *            optimistic locking version
     * @param lockId
     *            lock ID
     * @return dox meta with contents with "_id" and "_version" set.
     */
    DoxMeta update(String collectionName,
        DoxID id,
        JsonObject contents,
        int version,
        int lockId);

}
