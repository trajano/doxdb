package net.trajano.doxdb.sample.test;

import java.sql.Connection;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Resources;

import net.trajano.doxdb.jdbc.DoxPrincipal;
import net.trajano.doxdb.sample.SampleBean;

public class SampleEntityTest extends AbstractEntityTest {

    @Test
    public void testCrud() throws Exception {

        tx.begin();

        Connection connection = em.unwrap(Connection.class);
        Assert.assertNotNull(connection);

        SampleBean bean = new SampleBean(connection);
        bean.create(Resources.newInputStreamSupplier(Resources.getResource("sample.xml"))
                .getInput(), new DoxPrincipal("PRINCE"));
        tx.commit();
    }
}
