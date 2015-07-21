package net.trajano.doxdb.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.PersistenceException;

import net.trajano.doxdb.DoxConfiguration;
import net.trajano.doxdb.DoxDAO;
import net.trajano.doxdb.DoxFactory;

public class JdbcDoxFactory implements DoxFactory {

  final Map<String, JdbcDoxDAO> doxen = new ConcurrentHashMap<>();

  private final Connection c;

  public JdbcDoxFactory(Connection c, String... doxNames) {
    this.c = c;
    for (String doxName : doxNames) {
      DoxConfiguration configuration = new DoxConfiguration();
      configuration.setTableName(doxName);
      configuration.setHasOob(true);
      doxen.put(doxName, new JdbcDoxDAO(c, configuration));
    }
  }

  @Override
  public DoxDAO getDox(String name) {
    return doxen.get(name);
  }

  @Override
  public void close() {
    try {
      c.close();
    } catch (SQLException e) {
      throw new PersistenceException(e);
    }
  }

}
