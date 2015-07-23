package net.trajano.doxdb.spi;

import java.util.Map;

import net.trajano.doxdb.ConfigurationProvider;

public interface DoxProvider {

    ConfigurationProvider createDoxFactory(String name,
        Map<String, String> options);
}
