package net.trajano.doxdb.internal;

import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import net.trajano.doxdb.DoxFactory;
import net.trajano.doxdb.spi.DoxProvider;

/**
 * The provider is implemented as an EJB. This will read the Dox configuration
 * from META-INF/dox.xml of the current class loader. It is also responsible for
 * getting the JDBC connection from the default data source.
 * <p>
 * Can we have two EJB JARs? one with the customization and one with the
 * infrastructure?
 *
 * @author Archimedes
 */
public class DoxXmlConfiguredDoxProvider implements
    DoxProvider {

    @Override
    public DoxFactory createDoxFactory(final String name,
        final Map<String, String> options) {

        try {
            final Context ctx = new InitialContext();
            final DataSource dataSource = (DataSource) ctx.lookup("java:comp/DefaultDataSource");
            final String[] doxNames = {
                "Sample"
            };
            return null;
            //            return new JdbcDoxFactory(dataSource.getConnection(), doxNames);
        } catch (final NamingException e) {
            throw new PersistenceException(e);
        }
    }

}
