package net.trajano.doxdb;

public interface Dox {

    /**
     * Creates a dox record into the database. This will allocate a "_id" value
     * for the record.
     *
     * @param collectionName
     *            collection name
     * @param json
     *            dox contents
     * @return dox contents with "_id" set.
     */
    DocumentMeta create(String collectionName,
        String json);

    void delete(String collection,
        DoxID doxID,
        int version);

    /**
     * Does nothing, but calling it ensures that the EJB gets initialized.
     */
    void noop();

    DocumentMeta read(String collectionName,
        DoxID id);

    void reindex();

    DocumentMeta update(String collection,
        DoxID id,
        String json,
        int version);

}
