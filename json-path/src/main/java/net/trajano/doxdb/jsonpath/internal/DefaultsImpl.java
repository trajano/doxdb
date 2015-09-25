package net.trajano.doxdb.jsonpath.internal;

import java.util.EnumSet;
import java.util.Set;

import net.trajano.doxdb.jsonpath.Option;
import net.trajano.doxdb.jsonpath.Configuration.Defaults;
import net.trajano.doxdb.jsonpath.spi.json.JacksonJsonProvider;
import net.trajano.doxdb.jsonpath.spi.json.JsonProvider;
import net.trajano.doxdb.jsonpath.spi.mapper.JacksonMappingProvider;
import net.trajano.doxdb.jsonpath.spi.mapper.MappingProvider;

public final class DefaultsImpl implements
    Defaults {

    public static final DefaultsImpl INSTANCE = new DefaultsImpl();

    private final MappingProvider mappingProvider = new JacksonMappingProvider();

    private DefaultsImpl() {
    }

    @Override
    public JsonProvider jsonProvider() {

        return new JacksonJsonProvider();
    }

    @Override
    public MappingProvider mappingProvider() {

        return mappingProvider;
    }

    @Override
    public Set<Option> options() {

        return EnumSet.noneOf(Option.class);
    };

}
