package net.trajano.doxdb.ext;

import java.security.Principal;
import java.util.Map;

import net.trajano.doxdb.Dox;
import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxMeta;

public interface EventHandler {

    /**
     * Called when a {@link Dox} record is created.
     *
     * @param meta
     *            Dox meta data including the content.
     * @param content
     *            Dox content in JSON. Meta's content would have the _id and the
     *            _version.
     * @param extra
     *            extra data
     */
    void onRecordCreate(DoxMeta meta,
        String content,
        Map<String, String> extra);

    /**
     * Called when a {@link Dox} record is deleted.
     *
     * @param meta
     *            Dox meta data including the content.
     * @param content
     *            Dox content in JSON. Meta's content would have the _id and the
     *            _version.
     * @param extra
     *            extra data
     */
    void onRecordDelete(DoxMeta meta,
        String content,
        Map<String, String> extra);

    /**
     * Called when a {@link Dox} record is read.
     *
     * @param userPrincipal
     *            user principal
     * @param collectionName
     *            collection name
     * @param doxId
     *            Dox ID
     * @param json
     *            JSON of the record that was read.
     * @param extra
     *            extra data
     */
    void onRecordRead(Principal userPrincipal,
        String collectionName,
        DoxID doxId,
        String json);

    /**
     * Called when a {@link Dox} record is updated.
     *
     * @param meta
     *            Dox meta data including the content.
     * @param content
     *            Dox content in JSON. Meta's content would have the _id and the
     *            _version.
     * @param extra
     *            extra data
     */
    void onRecordUpdate(DoxMeta meta,
        String json,
        Map<String, String> extra);
}
