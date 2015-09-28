package net.trajano.doxdb.sample.test;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.PersistenceException;
import javax.validation.ValidationException;

import org.junit.Test;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxMeta;

public class DoxBeanTest extends AbstractBeanTest {

    @Test
    public void testCreate() throws Exception {

        {
            tx.begin();
            final String inputJson = "{\"name\":\"abc\"}";
            final DoxMeta meta = bean.create("horse", Json.createReader(new StringReader(inputJson)).readObject());

            assertEquals("abc", bean.read("horse", meta.getDoxId()).getContent().getString("name"));

            tx.commit();
        }
        {
            tx.begin();
            assertEquals("abc", bean.read("horse", bean.readByUniqueLookup("horse", "horseName", "abc").getDoxId()).getContent().getString("name"));
            tx.commit();
        }
        {
            tx.begin();
            final String inputJson = "{\"name\":\"xyz\"}";
            final DoxMeta meta = bean.create("horse", Json.createReader(new StringReader(inputJson)).readObject());

            assertEquals("xyz", bean.read("horse", meta.getDoxId()).getContent().getString("name"));
            tx.commit();
        }
        {
            tx.begin();

            assertEquals("abc", bean.read("horse", bean.readByUniqueLookup("horse", "horseName", "abc").getDoxId()).getContent().getString("name"));
            assertEquals("xyz", bean.read("horse", bean.readByUniqueLookup("horse", "horseName", "xyz").getDoxId()).getContent().getString("name"));
            tx.commit();
        }
    }

    @Test
    public void testCreateUpdate() throws Exception {

        final DoxID doxId;
        {
            tx.begin();
            final String inputJson = "{\"name\":\"abc\",\"fei\":\"abc\"}";
            final DoxMeta meta = bean.create("horse", Json.createReader(new StringReader(inputJson)).readObject());

            doxId = meta.getDoxId();
            assertEquals("abc", bean.read("horse", doxId).getContent().getString("name"));
            tx.commit();
        }
        {
            tx.begin();
            final String inputJson = "{\"name\":\"xyz\"}";
            final DoxMeta meta = bean.update("horse", doxId, Json.createReader(new StringReader(inputJson)).readObject(), 1);

            assertEquals(doxId, meta.getDoxId());
            assertEquals(2, meta.getVersion());
            assertEquals("xyz", bean.read("horse", doxId).getContent().getString("name"));
            tx.commit();
        }
    }

    @Test
    public void testCreateUpdateReindex() throws Exception {

        testCreateUpdate();
        {
            tx.begin();
            bean.reindex();
            tx.commit();
        }
    }

    @Test(expected = PersistenceException.class)
    public void testFailDuplicateCreate() throws Exception {

        tx.begin();
        final String inputJson = "{\"name\":\"abc\"}";
        bean.create("horse", Json.createReader(new StringReader(inputJson)).readObject());
        bean.create("horse", Json.createReader(new StringReader(inputJson)).readObject());
        tx.commit();
    }

    @Test(expected = PersistenceException.class)
    public void testFailDuplicateUpdate() throws Exception {

        final DoxID doxId;
        {
            tx.begin();
            final String inputJson = "{\"name\":\"abc\"}";
            final DoxMeta meta = bean.create("horse", Json.createReader(new StringReader(inputJson)).readObject());

            doxId = meta.getDoxId();
            assertEquals("abc", bean.read("horse", doxId).getContent().getString("name"));
            tx.commit();
        }
        {
            tx.begin();
            final String inputJson = "{\"name\":\"xyz\"}";
            final DoxMeta meta = bean.create("horse", Json.createReader(new StringReader(inputJson)).readObject());

            assertEquals("xyz", bean.read("horse", meta.getDoxId()).getContent().getString("name"));
            tx.commit();
        }
        {
            tx.begin();
            final String inputJson = "{\"name\":\"xyz\"}";
            final DoxMeta meta = bean.update("horse", doxId, Json.createReader(new StringReader(inputJson)).readObject(), 1);

            assertEquals(doxId, meta.getDoxId());
            assertEquals(2, meta.getVersion());
            assertEquals("xyz", bean.read("horse", doxId).getContent().getString("name"));
            tx.commit();
        }
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
