package net.trajano.doxdb.sample.test;

import java.sql.Connection;

import org.junit.Assert;
import org.junit.Test;

import net.trajano.doxdb.sample.SampleBean;

public class SampleEntityTest extends AbstractEntityTest {

    @Test
    public void testCrud() {

        tx.begin();

        Connection connection = em.unwrap(Connection.class);
        Assert.assertNotNull(connection);

        SampleBean bean = new SampleBean(connection);

        tx.commit();
    }
}
