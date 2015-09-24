package net.trajano.doxdb.sample.test;

import org.junit.Test;

/**
 * @author Archimedes Trajano
 */
public class JsonPathTest {

    // needs

    // <dependency>
    //    <groupId>com.jayway.jsonpath</groupId>
    //    <artifactId>json-path</artifactId>
    //    <version>2.0.1-SNAPSHOT</version>
    //    <exclusions>
    //        <exclusion>
    //            <groupId>net.minidev</groupId>
    //            <artifactId>json-smart</artifactId>
    //        </exclusion>
    //    </exclusions>
    // </dependency>

    @Test
    public void testSample() {

        //final Configuration conf = Configuration.builder().jsonProvider(new JacksonJsonProvider()).mappingProvider(new JacksonMappingProvider()).build();
        final String json = "[   {      \"name\" : \"john\",      \"gender\" : \"male\"   },   {      \"name\" : \"ben\"  }]";
        //final String gender0 = JsonPath.using(conf).parse(json).read("$[0]['gender']");
        //Assert.assertEquals("male", gender0);
    }
}
