package net.trajano.doxdb.ext;

import net.trajano.doxdb.DoxID;

public interface EventHandler {

    void onRecordCreate(String collection,
        DoxID doxid,
        String json);

    /**
     * @param collection
     * @param doxid
     * @param json
     *            JSON of the record that was deleted.
     */
    void onRecordDelete(String collection,
        DoxID doxid,
        String json);

    void onRecordRead(String collection,
        DoxID doxid,
        String json);

    void onRecordUpdate(String collection,
        DoxID doxid,
        String json);
}
