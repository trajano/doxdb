package net.trajano.doxdb.sample.test;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

import org.junit.Test;

import net.trajano.doxdb.Dox;

public class ContainerTest {

    @Test
    public void testCrud() throws Exception {

        final EJBContainer container = EJBContainer.createEJBContainer();
        System.out.println(container);
        final Context context = container.getContext();
        System.out.println(context);
        final Dox b = (Dox) context.lookup("java:global/ejb-app/classes/DoxBean");
        final String inputJson = "{\"name\":\"abc\"}";
        System.out.println(b.create("horse", inputJson));

    }
}
