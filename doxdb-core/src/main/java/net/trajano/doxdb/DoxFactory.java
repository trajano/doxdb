package net.trajano.doxdb;

public interface DoxFactory {

    DoxDAO getDox(String name);

    void close();
}
