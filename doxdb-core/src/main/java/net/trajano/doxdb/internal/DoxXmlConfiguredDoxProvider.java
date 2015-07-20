package net.trajano.doxdb.internal;

import java.util.Map;

import net.trajano.doxdb.DoxFactory;
import net.trajano.doxdb.jdbc.JdbcDoxFactory;
import net.trajano.doxdb.spi.DoxProvider;

/**
 * This will read the Dox configuration from META-INF/dox.xml of the current
 * class loader.
 *
 * @author Archimedes
 */
public class DoxXmlConfiguredDoxProvider implements DoxProvider {

  @Override
  public DoxFactory createDoxFactory(String name, Map<String, String> options) {
    return new JdbcDoxFactory();
  }

}
