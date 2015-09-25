package net.trajano.doxdb.sample.test;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.validation.ValidationException;

import org.junit.Test;

import net.trajano.doxdb.DoxMeta;

public class DoxBeanTest extends AbstractBeanTest {

    @Test
    public void testCrud() throws Exception {

        tx.begin();
        final String inputJson = "{\"name\":\"abc\"}";
        final DoxMeta meta = bean.create("horse", Json.createReader(new StringReader(inputJson)).readObject());

        assertEquals("abc", bean.read("horse", meta.getDoxId()).getContent().getString("name"));
        tx.commit();
    }

    @Test(expected = ValidationException.class)
    public void testFailValidation() throws Exception {

        tx.begin();
        final String inputJson = "{\"noname\":\"abc\"}";
        final JsonObject json = Json.createReader(new StringReader(inputJson)).readObject();
        bean.create("horse", json);
        tx.commit();

    }
}
