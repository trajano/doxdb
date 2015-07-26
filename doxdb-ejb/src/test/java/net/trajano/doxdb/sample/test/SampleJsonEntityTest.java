package net.trajano.doxdb.sample.test;

import java.io.StringReader;
import java.sql.Connection;

import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.PersistenceException;

import org.junit.Assert;
import org.junit.Test;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.ejb.DoxBean;
import net.trajano.doxdb.internal.DoxPrincipal;

public class SampleJsonEntityTest extends AbstractEntityTest {

    @Test
    public void testCrud() throws Exception {

        tx.begin();

        final Connection connection = em.unwrap(Connection.class);
        Assert.assertNotNull(connection);

        final DoxBean bean = new DoxBean();
        bean.setConnection(connection);
        bean.init();

        final String inputJson = "{\"name\":\"abc\"}";
        final JsonObject o = Json.createReader(new StringReader(inputJson))
            .readObject();
        final DoxID id = bean.create(o, new DoxPrincipal("PRINCE"));
        Assert.assertEquals(inputJson, bean.readContent(id)
            .toString());
        tx.commit();
    }

    @Test(expected = PersistenceException.class)
    public void testFailValidation() throws Exception {

        tx.begin();

        final Connection connection = em.unwrap(Connection.class);
        Assert.assertNotNull(connection);

        final DoxBean bean = new DoxBean();
        bean.setConnection(connection);
        bean.init();

        final String inputJson = "{\"noname\":\"abc\"}";
        final JsonObject o = Json.createReader(new StringReader(inputJson))
            .readObject();
        final DoxID id = bean.create(o, new DoxPrincipal("PRINCE"));
        Assert.assertEquals(inputJson, bean.readContent(id)
            .toString());
        tx.commit();
    }
}
