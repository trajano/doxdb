package net.trajano.doxdb.sample.test;

import static org.junit.Assert.assertEquals;

import javax.validation.ValidationException;

import org.bson.BsonDocument;
import org.junit.Test;

import net.trajano.doxdb.DoxMeta;

public class DoxBeanTest extends AbstractBeanTest {

    @Test
    public void testCrud() throws Exception {

        tx.begin();
        final String inputJson = "{\"name\":\"abc\"}";
        final BsonDocument bson = BsonDocument.parse(inputJson);
        final DoxMeta meta = bean.create("horse", bson);

        final BsonDocument readBson = BsonDocument.parse(bean.read("horse", meta.getDoxId())
            .getContentJson());

        assertEquals(bson.getString("name"), readBson.getString("name"));
        tx.commit();
    }

    @Test(expected = ValidationException.class)
    public void testFailValidation() throws Exception {

        tx.begin();
        final String inputJson = "{\"noname\":\"abc\"}";
        final BsonDocument bson = BsonDocument.parse(inputJson);
        bean.create("horse", bson);
        tx.commit();

    }
}
