package net.trajano.doxdb.internal;

import java.sql.SQLException;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import net.trajano.doxdb.DoxFactory;
import net.trajano.doxdb.jdbc.JdbcDoxFactory;
import net.trajano.doxdb.spi.DoxProvider;

/**
 * This will read the Dox configuration from META-INF/dox.xml of the current
 * class loader. It is also responsible for getting the JDBC connection from the
 * default data source.
 *
 * @author Archimedes
 */
public class DoxXmlConfiguredDoxProvider implements DoxProvider {

  @Override
  public DoxFactory createDoxFactory(String name, Map<String, String> options) {
    try {
      Context ctx = new InitialContext();
      DataSource dataSource = (DataSource) ctx.lookup("java:comp/DefaultDataSource");
      String[] doxNames = { "Sample" };
      return new JdbcDoxFactory(dataSource.getConnection(), doxNames);
    } catch (SQLException | NamingException e) {
      throw new PersistenceException(e);
    }
  }

}
