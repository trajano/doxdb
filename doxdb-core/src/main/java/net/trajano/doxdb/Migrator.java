package net.trajano.doxdb;

public interface Migrator {

    String migrate(String collection,
        int oldContentVersion,
        int newContentVersion,
        String json);
}
