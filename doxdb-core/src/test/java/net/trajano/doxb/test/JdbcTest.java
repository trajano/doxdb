package net.trajano.doxb.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.persistence.PersistenceException;

import org.junit.Assert;
import org.junit.Test;

import net.trajano.commons.testing.ResourceUtil;
import net.trajano.doxdb.DoxConfiguration;
import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.jdbc.DoxPrincipal;
import net.trajano.doxdb.jdbc.JdbcDoxDAO;

public class JdbcTest {

    @Test
    public void testDoubleCreateDerby() throws Exception {

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        final Connection c = DriverManager.getConnection("jdbc:derby:memory:" + DoxID.generate() + ";create=true");

        // Connection c = DriverManager.getConnection("jdbc:derby://" +
        // InetAddress.getLocalHost()
        // .getHostName() +
        // ":1527/sun-appserv-samples;create=true;upgrade=true");

        final JdbcDoxDAO dao = new JdbcDoxDAO(c, "sample");
        final JdbcDoxDAO dao2 = new JdbcDoxDAO(c, "sample");
        final DoxID d1 = dao.create(ResourceUtil.getResourceAsStream("sample.xml"), 1, new DoxPrincipal("PRINCE"));
        final DoxID d2 = dao2.create(ResourceUtil.getResourceAsStream("sample.xml"), 1, new DoxPrincipal("PRINCE"));
        assertFalse(d1.equals(d2));

        final ByteArrayOutputStream content1 = new ByteArrayOutputStream();
        dao.readContentToStream(d1, content1);
        content1.close();

        final ByteArrayOutputStream content2 = new ByteArrayOutputStream();
        dao.readContentToStream(d2, content2);
        content2.close();

        final ByteArrayOutputStream content3 = new ByteArrayOutputStream();
        dao2.readContentToStream(d1, content3);
        content3.close();

        final ByteArrayOutputStream content4 = new ByteArrayOutputStream();
        dao2.readContentToStream(d2, content4);
        content4.close();

        assertEquals(content1.toString(), content3.toString());
        assertEquals(content1.toString(), content2.toString());
        assertEquals(content2.toString(), content3.toString());
        assertEquals(content3.toString(), content4.toString());
    }

    @Test
    public void testDoubleCreateDerbyWithOOB() throws Exception {

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        final Connection c = DriverManager.getConnection("jdbc:derby:memory:" + DoxID.generate() + ";create=true");

        final DoxConfiguration config = new DoxConfiguration("sample");
        config.setHasOob(true);

        // Connection c = DriverManager.getConnection("jdbc:derby://" +
        // InetAddress.getLocalHost()
        // .getHostName() +
        // ":1527/sun-appserv-samples;create=true;upgrade=true");

        final JdbcDoxDAO dao = new JdbcDoxDAO(c, config);
        final JdbcDoxDAO dao2 = new JdbcDoxDAO(c, config);
        final DoxID d1 = dao.create(ResourceUtil.getResourceAsStream("sample.xml"), 1, new DoxPrincipal("PRINCE"));
        final DoxID d2 = dao2.create(ResourceUtil.getResourceAsStream("sample.xml"), 1, new DoxPrincipal("PRINCE"));
        Assert.assertFalse(d1.equals(d2));
    }

    @Test
    public void testDoubleCreateH2() throws Exception {

        Class.forName("org.h2.Driver");

        // Connection c = DriverManager.getConnection("jdbc:derby://" +
        // InetAddress.getLocalHost()
        // .getHostName() +
        // ":1527/sun-appserv-samples;create=true;upgrade=true");
        final DoxID d1;
        {
            final Connection c = DriverManager.getConnection("jdbc:h2:mem:test");
            final JdbcDoxDAO dao = new JdbcDoxDAO(c, "sample");
            d1 = dao.create(ResourceUtil.getResourceAsStream("sample.xml"), 1, new DoxPrincipal("PRINCE"));
            c.commit();
            c.close();
        }

        {
            final Connection c = DriverManager.getConnection("jdbc:h2:mem:test");
            final JdbcDoxDAO dao2 = new JdbcDoxDAO(c, "sample");
            final DoxID d2 = dao2.create(ResourceUtil.getResourceAsStream("sample.xml"), 1, new DoxPrincipal("PRINCE"));
            Assert.assertFalse(d1.equals(d2));
        }
    }

    @Test
    public void testExportImport() throws Exception {

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        final Connection c = DriverManager.getConnection("jdbc:derby:memory:" + DoxID.generate() + ";create=true");

        // Connection c = DriverManager.getConnection("jdbc:derby://" +
        // InetAddress.getLocalHost()
        // .getHostName() +
        // ":1527/sun-appserv-samples;create=true;upgrade=true");

        final JdbcDoxDAO dao = new JdbcDoxDAO(c, "sample");
        final DoxID d1 = dao.create(ResourceUtil.getResourceAsStream("sample.xml"), 1, new DoxPrincipal("PRINCE"));

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dao.exportDox(d1, baos);
        baos.close();

        assertEquals(1, dao.getContentVersion(d1));
        dao.delete(d1, dao.getVersion(d1), new DoxPrincipal("PRINCE"));

        dao.importDox(new ByteArrayInputStream(baos.toByteArray()), 1);

        final byte[] buffer0 = ResourceUtil.getResourceAsBytes("sample.xml");
        final byte[] buffer1 = new byte[buffer0.length];
        dao.readContent(d1, ByteBuffer.wrap(buffer1));
        assertArrayEquals(buffer0, buffer1);
        assertEquals(1, dao.getContentVersion(d1));
        c.commit();
        c.close();
    }

    @Test(expected = PersistenceException.class)
    public void testFailUpdateSmallerLob() throws Exception {

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        final Connection c = DriverManager.getConnection("jdbc:derby:memory:" + DoxID.generate() + ";create=true");

        // Connection c = DriverManager.getConnection("jdbc:derby://" +
        // InetAddress.getLocalHost()
        // .getHostName() +
        // ":1527/sun-appserv-samples;create=true;upgrade=true");

        final DoxConfiguration config = new DoxConfiguration("sample");
        config.setLobSize(5000);
        final JdbcDoxDAO dao = new JdbcDoxDAO(c, config);
        final DoxID d1 = dao.create(ResourceUtil.getResourceAsStream("sample.xml"), 1, new DoxPrincipal("PRINCE"));
        final DoxID d2 = dao.create(ResourceUtil.getResourceAsStream("sample.xml"), 1, new DoxPrincipal("PRINCE"));
        Assert.assertFalse(d1.equals(d2));

        dao.updateContent(d1, ResourceUtil.getResourceAsStream("sample.bin"), 1, dao.getVersion(d1), new DoxPrincipal("PRINCEUP"));

        final byte[] buffer0 = ResourceUtil.getResourceAsBytes("sample.bin");
        final byte[] buffer1 = new byte[buffer0.length];
        dao.readContent(d1, ByteBuffer.wrap(buffer1));
        assertArrayEquals(buffer0, buffer1);
        final int d1Version = dao.getVersion(d1);
        dao.delete(d1, d1Version, new DoxPrincipal("PRINCE"));
        c.commit();
        c.close();
    }

    @Test
    public void testPersistence() throws Exception {

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        final Connection c = DriverManager.getConnection("jdbc:derby:memory:" + DoxID.generate() + ";create=true");

        // Connection c = DriverManager.getConnection("jdbc:derby://" +
        // InetAddress.getLocalHost()
        // .getHostName() +
        // ":1527/sun-appserv-samples;create=true;upgrade=true");

        final JdbcDoxDAO dao = new JdbcDoxDAO(c, "sample");
        final DoxID d1 = dao.create(ResourceUtil.getResourceAsStream("sample.bin"), 1, new DoxPrincipal("PRINCE"));
        final DoxID d2 = dao.create(ResourceUtil.getResourceAsStream("sample.bin"), 1, new DoxPrincipal("PRINCE"));
        Assert.assertFalse(d1.equals(d2));
        final byte[] buffer2 = ResourceUtil.getResourceAsBytes("sample.bin");

        final byte[] buffer1 = new byte[buffer2.length];
        final int size = dao.readContent(d1, ByteBuffer.wrap(buffer1));

        assertEquals(buffer2.length, size);

        Assert.assertArrayEquals(buffer1, buffer2);
        final int d1Version = dao.getVersion(d1);
        dao.delete(d1, d1Version, new DoxPrincipal("PRINCE"));
        c.commit();
        c.close();
    }

    @Test
    public void testUpdate() throws Exception {

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        final Connection c = DriverManager.getConnection("jdbc:derby:memory:" + DoxID.generate() + ";create=true");

        final JdbcDoxDAO dao = new JdbcDoxDAO(c, "sample");
        final DoxID d1 = dao.create(ResourceUtil.getResourceAsStream("sample.xml"), 1, new DoxPrincipal("PRINCE"));
        final DoxID d2 = dao.create(ResourceUtil.getResourceAsStream("sample.xml"), 1, new DoxPrincipal("PRINCE"));
        Assert.assertFalse(d1.equals(d2));

        dao.updateContent(d1, ResourceUtil.getResourceAsStream("sample.bin"), 1, dao.getVersion(d1), new DoxPrincipal("PRINCEUP"));

        final byte[] buffer1 = ResourceUtil.getResourceAsBytes("sample.bin");
        dao.readContent(d1, ByteBuffer.wrap(buffer1));
        final byte[] buffer2 = ResourceUtil.getResourceAsBytes("sample.bin");
        assertArrayEquals(buffer1, buffer2);
        final int d1Version = dao.getVersion(d1);
        dao.delete(d1, d1Version, new DoxPrincipal("PRINCE"));
        c.commit();
        c.close();
    }

    @Test
    public void testUpdateSmallerLob() throws Exception {

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        final Connection c = DriverManager.getConnection("jdbc:derby:memory:" + DoxID.generate() + ";create=true");

        final DoxConfiguration config = new DoxConfiguration("sample");
        config.setLobSize(500000);
        final JdbcDoxDAO dao = new JdbcDoxDAO(c, config);
        final DoxID d1 = dao.create(ResourceUtil.getResourceAsStream("sample.xml"), 1, new DoxPrincipal("PRINCE"));
        final DoxID d2 = dao.create(ResourceUtil.getResourceAsStream("sample.xml"), 1, new DoxPrincipal("PRINCE"));
        Assert.assertFalse(d1.equals(d2));

        dao.updateContent(d1, ResourceUtil.getResourceAsStream("sample.bin"), 1, dao.getVersion(d1), new DoxPrincipal("PRINCEUP"));

        final byte[] buffer0 = ResourceUtil.getResourceAsBytes("sample.bin");
        final byte[] buffer1 = new byte[buffer0.length];
        dao.readContent(d1, ByteBuffer.wrap(buffer1));
        assertArrayEquals(buffer0, buffer1);
        final int d1Version = dao.getVersion(d1);
        dao.delete(d1, d1Version, new DoxPrincipal("PRINCE"));
        c.commit();
        c.close();
    }
}
