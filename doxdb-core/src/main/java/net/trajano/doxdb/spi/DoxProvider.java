package net.trajano.doxdb.spi;

import java.util.Map;

import net.trajano.doxdb.DoxFactory;

public interface DoxProvider {
  DoxFactory createDoxFactory(String name, Map<String, String> options);
}
