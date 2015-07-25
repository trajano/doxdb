package net.trajano.doxdb.sample.test;

import java.io.InputStream;
import java.sql.Connection;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Resources;

import net.trajano.doxdb.internal.DoxPrincipal;
import net.trajano.doxdb.sampleejb.SampleBean;

public class SampleEntityTest extends AbstractEntityTest {

    @Test
    public void testCrud() throws Exception {

        tx.begin();

        Connection connection = em.unwrap(Connection.class);
        Assert.assertNotNull(connection);

        try (SampleBean bean = new SampleBean()) {
            bean.setConnection(connection);
            bean.init();
            try (InputStream is = Resources.getResource("sample.xml")
                    .openStream()) {
                bean.create(is, new DoxPrincipal("PRINCE"));
            }
        }
        tx.commit();
    }
}
