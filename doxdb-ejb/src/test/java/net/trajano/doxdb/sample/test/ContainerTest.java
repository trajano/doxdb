package net.trajano.doxdb.sample.test;

import java.io.StringReader;

import javax.ejb.embeddable.EJBContainer;
import javax.json.Json;
import javax.json.JsonObject;
import javax.naming.Context;

import org.junit.Test;

import net.trajano.doxdb.ejb.SampleJsonBean;
import net.trajano.doxdb.jdbc.DoxPrincipal;

public class ContainerTest {

    @Test
    public void testCrud() throws Exception {

        EJBContainer container = EJBContainer.createEJBContainer();
        System.out.println(container);
        Context context = container.getContext();
        System.out.println(context);
        SampleJsonBean b = (SampleJsonBean) context.lookup("java:global/classes/SampleJsonBean");
        String inputJson = "{\"name\":\"abc\"}";
        JsonObject o = Json.createReader(new StringReader(inputJson))
                .readObject();
        System.out.println(b.create(o, new DoxPrincipal("PRINCE")));

    }
}
