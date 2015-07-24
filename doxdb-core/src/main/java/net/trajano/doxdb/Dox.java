package net.trajano.doxdb;

public interface Dox {

    DoxID create(String collectionName,
        String json);

    /**
     * Does nothing, but calling it ensures that the EJB gets initialized.
     */
    void noop();

}
