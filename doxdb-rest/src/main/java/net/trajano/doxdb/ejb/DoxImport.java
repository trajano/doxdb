package net.trajano.doxdb.ejb;

import java.util.Date;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface DoxImport {

    JsonObject exportDox(String exportPath,
        String schema,
        Date fromLastUpdatedOn);

    JsonObject importDox(String importPath);

}
