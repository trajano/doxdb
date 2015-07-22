package net.trajano.doxdb.sample.test;

import java.io.StringReader;
import java.sql.Connection;

import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.PersistenceException;

import org.junit.Assert;
import org.junit.Test;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.jdbc.DoxPrincipal;
import net.trajano.doxdb.sampleejb.SampleJsonBean;

public class SampleJsonEntityTest extends AbstractEntityTest {

    @Test
    public void testCrud() throws Exception {

        tx.begin();

        Connection connection = em.unwrap(Connection.class);
        Assert.assertNotNull(connection);

        SampleJsonBean bean = new SampleJsonBean();
        bean.setConnection(connection);
        bean.init();

        String inputJson = "{\"name\":\"abc\"}";
        JsonObject o = Json.createReader(new StringReader(inputJson))
                .readObject();
        DoxID id = bean.create(o, new DoxPrincipal("PRINCE"));
        Assert.assertEquals(inputJson, bean.readContent(id)
                .toString());
        tx.commit();
    }

    @Test(expected = PersistenceException.class)
    public void testFailValidation() throws Exception {

        tx.begin();

        Connection connection = em.unwrap(Connection.class);
        Assert.assertNotNull(connection);

        SampleJsonBean bean = new SampleJsonBean();
        bean.setConnection(connection);
        bean.init();

        String inputJson = "{\"noname\":\"abc\"}";
        JsonObject o = Json.createReader(new StringReader(inputJson))
                .readObject();
        DoxID id = bean.create(o, new DoxPrincipal("PRINCE"));
        Assert.assertEquals(inputJson, bean.readContent(id)
                .toString());
        tx.commit();
    }
}
