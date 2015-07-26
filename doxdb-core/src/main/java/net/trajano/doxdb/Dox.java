package net.trajano.doxdb;

import org.bson.BsonDocument;

public interface Dox {

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

    void delete(String collection,
        DoxID doxID,
        int version);

    /**
     * Does nothing, but calling it ensures that the EJB gets initialized.
     */
    void noop();

    DoxMeta read(String collectionName,
        DoxID id);

    BsonDocument readAll(String collection);

    void reindex();

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
