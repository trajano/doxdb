package net.trajano.doxdb.sample.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

import org.bson.BsonDocument;
import org.junit.Test;

import net.trajano.doxdb.Dox;
import net.trajano.doxdb.DoxMeta;

public class ContainerTest {

    @Test
    public void testCrud() throws Exception {

        final EJBContainer container = EJBContainer.createEJBContainer();
        assertNotNull(container);
        final Context context = container.getContext();
        assertNotNull(context);
        final Dox b = (Dox) context.lookup("java:global/ejb-app/classes/DoxBean");
        final String inputJson = "{\"name\":\"abc\"}";
        final DoxMeta created = b.create("horse", BsonDocument.parse(inputJson));
        final BsonDocument createdBson = BsonDocument.parse(created.getContentJson());
        assertNotNull(created.getDoxId());
        assertEquals(1, created.getVersion());
        assertEquals("abc", createdBson.getString("name").getValue());

    }
}
