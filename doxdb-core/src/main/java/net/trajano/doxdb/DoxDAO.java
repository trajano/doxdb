package net.trajano.doxdb;

import java.io.InputStream;
import java.security.Principal;

public interface DoxDAO {

    DoxID create(InputStream in,
            Principal principal);

    int getVersion(DoxID id);

    InputStream readOobContent(DoxID doxId,
            String reference);

    InputStream readContent(DoxID id);

    void delete(DoxID id,
            int version,
            Principal principal);

    void detach(DoxID doxId,
            String reference,
            int version,
            Principal principal);

    void attach(DoxID doxId,
            String reference,
            InputStream in,
            int version,
            Principal principal);

    void updateContent(DoxID doxId,
            InputStream contentStream,
            int version,
            Principal principal);

}
