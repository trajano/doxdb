package net.trajano.doxdb.sample.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import net.trajano.doxdb.jsonpath.Configuration;
import net.trajano.doxdb.jsonpath.JsonPath;
import net.trajano.doxdb.jsonpath.spi.json.JacksonJsonProvider;
import net.trajano.doxdb.jsonpath.spi.mapper.JacksonMappingProvider;

/**
 * @author Archimedes Trajano
 */
public class JsonPathTest {

    /**
     * Ensure that a newly created directory will not return anything in the
     * iterator.
     *
     * @throws Exception
     */
    @Test
    public void testDirectory() throws Exception {

        final Path createTempDirectory = Files.createTempDirectory(null);
        assertFalse(Files.newDirectoryStream(createTempDirectory).iterator().hasNext());
        Files.deleteIfExists(createTempDirectory);
    }

    @Test
    public void testSample() {

        final Configuration conf = Configuration.builder().jsonProvider(new JacksonJsonProvider()).mappingProvider(new JacksonMappingProvider()).build();
        final String json = "[   {      \"name\" : \"john\",      \"gender\" : \"male\"   },   {      \"name\" : \"ben\"  }]";
        final String gender0 = JsonPath.using(conf).parse(json).read("$[0]['gender']");
        assertEquals("male", gender0);
    }
}
