package net.trajano.doxdb.ext;

import java.util.Map;

import net.trajano.doxdb.Dox;
import net.trajano.doxdb.DoxID;

public interface EventHandler {

    /**
     * Called when a {@link Dox} record is created.
     *
     * @param collection
     *            collection name
     * @param doxId
     *            Dox ID
     * @param json
     *            JSON of the record that was created.
     * @param extra
     *            extra data
     */
    void onRecordCreate(String collectionName,
        DoxID doxId,
        String json,
        Map<String, String> extra);

    /**
     * Called when a {@link Dox} record is deleted.
     *
     * @param collectionName
     *            collection name
     * @param doxId
     *            Dox ID
     * @param json
     *            JSON of the record that was deleted.
     * @param extra
     *            extra data
     */
    void onRecordDelete(String collectionName,
        DoxID doxId,
        String json,
        Map<String, String> extra);

    /**
     * Called when a {@link Dox} record is read.
     *
     * @param collectionName
     *            collection name
     * @param doxId
     *            Dox ID
     * @param json
     *            JSON of the record that was read.
     * @param extra
     *            extra data
     */
    void onRecordRead(String collectionName,
        DoxID doxId,
        String json);

    /**
     * Called when a {@link Dox} record is updated.
     *
     * @param collectionName
     *            collection name
     * @param doxId
     *            Dox ID
     * @param json
     *            JSON of the record that was updated.
     * @param extra
     *            extra data
     */
    void onRecordUpdate(String collectionName,
        DoxID doxId,
        String json,
        Map<String, String> extra);
}
