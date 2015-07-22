package net.trajano.doxdb;

import java.util.SortedMap;

import javax.sql.DataSource;

import net.trajano.doxdb.schema.DoxType;
import net.trajano.doxdb.schema.SchemaType;

/**
 * This will be an SLSB. There should be many instances of this and should be
 * able to spread through the EJB pool.
 *
 * @author trajanar
 */
public class JsonDox implements
    Dox {

    private final DoxType config;

    private final DataSource ds;

    private SortedMap<Integer, SchemaType> schemas;

    public JsonDox(final DataSource ds,
        final DoxType config) {
        this.ds = ds;
        this.config = config;
        for (final SchemaType st : config.getSchema()) {
            // load JSON schema
        }
    }

}
