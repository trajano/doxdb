package net.trajano.doxdb.spi;

public interface Migrator {

    String migrate(String collection,
        int oldContentVersion,
        int newContentVersion,
        String json);
}
