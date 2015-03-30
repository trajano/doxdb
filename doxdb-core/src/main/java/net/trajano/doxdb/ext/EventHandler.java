package net.trajano.doxdb.ext;

import net.trajano.doxdb.DoxID;

public interface EventHandler {

    void onRecordCreate(String collection,
        DoxID doxid,
        String json);

    void onRecordDelete(String collection,
        DoxID doxid);

    void onRecordRead(String collection,
        DoxID doxid,
        String json);

    void onRecordUpdate(String collection,
        DoxID doxid,
        String json);
}
