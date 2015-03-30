package net.trajano.doxdb.ext;

public interface Migrator {

    String migrate(String collection,
        int oldContentVersion,
        int newContentVersion,
        String json);
}
