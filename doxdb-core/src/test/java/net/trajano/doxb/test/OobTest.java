package net.trajano.doxb.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.ByteStreams;

import net.trajano.commons.testing.ResourceUtil;
import net.trajano.doxdb.DoxConfiguration;
import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.internal.DoxPrincipal;
import net.trajano.doxdb.jdbc.JdbcDoxDAO;

public class OobTest {

    static byte[] sample1;

    static byte[] sample2;

    private final ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);

    private Connection c;

    @Before
    public void setupJdbc() throws Exception {

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        c = DriverManager.getConnection("jdbc:derby:memory:" + DoxID.generate() + ";create=true");

    }

    @After
    public void tearDownJdbc() throws Exception {

        c.close();
    }

    /**
     * Detaching an OOB will move it to the tombstone.
     *
     * @throws Exception
     */
    @Test
    public void testDetachOob() throws Exception {

        final DoxConfiguration doxConfiguration = new DoxConfiguration();
        doxConfiguration.setTableName("sample");
        doxConfiguration.setHasOob(true);
        final JdbcDoxDAO dao = new JdbcDoxDAO(c, doxConfiguration);

        final DoxID d1 = dao.create(ResourceUtil.getResourceAsStream("sample.bin"), 1, new DoxPrincipal("PRINCE"));
        dao.create(ResourceUtil.getResourceAsStream("sample.bin"), 1, new DoxPrincipal("PRINCE"));

        try {
            dao.readOobContentToStream(d1, "ref", new ByteArrayOutputStream());
            Assert.fail();
        } catch (final EntityNotFoundException e) {
            // Expected
        }

        final int version1 = dao.getVersion(d1);
        dao.attach(d1, "ref", ResourceUtil.getResourceAsStream("sample.bin"), version1, new DoxPrincipal("PRINSIPE"));

        dao.readOobContentToStream(d1, "ref", new ByteArrayOutputStream());
        Assert.assertNotNull(dao.readOobContent(d1, "ref", buffer));

        final int version2 = dao.getVersion(d1);
        dao.detach(d1, "ref", version2, new DoxPrincipal("PRINSIPEUP"));

        Assert.assertTrue(version2 > version1);

        try {
            dao.readOobContentToStream(d1, "ref", new ByteArrayOutputStream());
            Assert.fail();
        } catch (final EntityNotFoundException e) {
            // Expected
        }
        dao.readContentToStream(d1, new ByteArrayOutputStream());
        c.commit();
    }

    /**
     * Detaching an OOB will move it to the tombstone.
     *
     * @throws Exception
     */
    @Test
    public void testDetachUpdateOob() throws Exception {

        final DoxConfiguration doxConfiguration = new DoxConfiguration();
        doxConfiguration.setTableName("sample");
        doxConfiguration.setHasOob(true);
        final JdbcDoxDAO dao = new JdbcDoxDAO(c, doxConfiguration);

        final DoxID d1 = dao.create(ResourceUtil.getResourceAsStream("sample.bin"), 1, new DoxPrincipal("PRINCE"));
        dao.create(ResourceUtil.getResourceAsStream("sample.bin"), 1, new DoxPrincipal("PRINCE"));

        try {
            dao.readOobContentToStream(d1, "ref", new ByteArrayOutputStream());
            Assert.fail();
        } catch (final EntityNotFoundException e) {
            // Expected
        }

        final int version1 = dao.getVersion(d1);
        dao.attach(d1, "ref", ResourceUtil.getResourceAsStream("sample.bin"), version1, new DoxPrincipal("PRINSIPE"));

        Assert.assertNotNull(dao.readOobContent(d1, "ref", buffer));

        final int version2 = dao.getVersion(d1);
        dao.detach(d1, "ref", version2, new DoxPrincipal("PRINSIPEDEL"));

        Assert.assertTrue(version2 > version1);

        try {
            dao.readOobContent(d1, "ref", buffer);
            Assert.fail();
        } catch (final EntityNotFoundException e) {
            // Expected
        }

        final int version3 = dao.getVersion(d1);
        dao.attach(d1, "ref", ResourceUtil.getResourceAsStream("sample.xml"), version3, new DoxPrincipal("PRINSIPEUP"));

        Assert.assertTrue(String.format("Expected versions %d > %d", version3, version2), version3 > version2);
        dao.readContent(d1, buffer);

        final byte[] buffer1 = new byte[200];
        dao.readOobContent(d1, "ref", ByteBuffer.wrap(buffer1));

        final byte[] buffer2 = new byte[200];
        ByteStreams.readFully(ResourceUtil.getResourceAsStream("sample.xml"), buffer2);
        Assert.assertArrayEquals(buffer1, buffer2);

        final int d1Version = dao.getVersion(d1);
        Assert.assertTrue(d1Version > version3);
        dao.delete(d1, d1Version, new DoxPrincipal("PRINCE"));

        c.commit();
    }

    @Test
    public void testExportImport() throws Exception {

        final DoxConfiguration doxConfiguration = new DoxConfiguration();
        doxConfiguration.setTableName("sample");
        doxConfiguration.setHasOob(true);
        final JdbcDoxDAO dao = new JdbcDoxDAO(c, doxConfiguration);
        final DoxID d1 = dao.create(ResourceUtil.getResourceAsStream("sample.bin"), 1, new DoxPrincipal("PRINCE"));
        dao.attach(d1, "ref", ResourceUtil.getResourceAsStream("sample.bin"), dao.getVersion(d1), new DoxPrincipal("PRINSIPE"));

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dao.exportDox(d1, baos);
        baos.close();

        dao.delete(d1, dao.getVersion(d1), new DoxPrincipal("PRINCE"));

        dao.importDox(new ByteArrayInputStream(baos.toByteArray()), 1);

        final byte[] buffer1 = new byte[200];
        dao.readContent(d1, ByteBuffer.wrap(buffer1));
        final byte[] buffer2 = new byte[200];
        ByteStreams.readFully(ResourceUtil.getResourceAsStream("sample.bin"), buffer2);

        Assert.assertArrayEquals(buffer2, buffer1);

        final byte[] oobBuffer1 = new byte[200];
        dao.readOobContent(d1, "ref", ByteBuffer.wrap(oobBuffer1));
        final byte[] oobBuffer2 = new byte[200];
        ByteStreams.readFully(ResourceUtil.getResourceAsStream("sample.bin"), oobBuffer2);
        Assert.assertArrayEquals(oobBuffer2, oobBuffer1);

        c.commit();
        c.close();
    }

    /**
     * When updating an OOB entry it will replace it in place. It won't keep the
     * history. Will fail because the size is too small.
     *
     * @throws Exception
     */
    @Test(expected = PersistenceException.class)
    public void testFailUpdateOobSmallerLOB() throws Exception {

        final DoxConfiguration doxConfiguration = new DoxConfiguration();
        doxConfiguration.setTableName("sample");
        doxConfiguration.setHasOob(true);
        doxConfiguration.setOobLobSize(4200);
        final JdbcDoxDAO dao = new JdbcDoxDAO(c, doxConfiguration);
        final DoxID d1 = dao.create(ResourceUtil.getResourceAsStream("sample.bin"), 1, new DoxPrincipal("PRINCE"));
        dao.create(ResourceUtil.getResourceAsStream("sample.bin"), 1, new DoxPrincipal("PRINCE"));
        final int version1 = dao.getVersion(d1);
        dao.attach(d1, "ref", ResourceUtil.getResourceAsStream("sample.bin"), version1, new DoxPrincipal("PRINSIPE"));

        final int version2 = dao.getVersion(d1);
        dao.attach(d1, "ref", ResourceUtil.getResourceAsStream("sample.xml"), version2, new DoxPrincipal("PRINSIPEUP"));

        Assert.assertTrue(version2 > version1);
        final byte[] buffer1 = new byte[200];
        dao.readOobContent(d1, "ref", ByteBuffer.wrap(buffer1));

        final byte[] buffer2 = new byte[200];
        ByteStreams.readFully(ResourceUtil.getResourceAsStream("sample.xml"), buffer2);
        Assert.assertArrayEquals(buffer1, buffer2);
        final int d1Version = dao.getVersion(d1);
        Assert.assertTrue(d1Version > version2);
        dao.delete(d1, d1Version, new DoxPrincipal("PRINCE"));
        c.commit();
    }

    @Test
    public void testOobPersistence() throws Exception {

        final DoxConfiguration doxConfiguration = new DoxConfiguration();
        doxConfiguration.setTableName("sample");
        doxConfiguration.setHasOob(true);
        final JdbcDoxDAO dao = new JdbcDoxDAO(c, doxConfiguration);
        final DoxID d1 = dao.create(ResourceUtil.getResourceAsStream("sample.bin"), 1, new DoxPrincipal("PRINCE"));
        dao.create(ResourceUtil.getResourceAsStream("sample.bin"), 1, new DoxPrincipal("PRINCE"));
        dao.attach(d1, "ref", ResourceUtil.getResourceAsStream("sample.xml"), dao.getVersion(d1), new DoxPrincipal("PRINSIPE"));

        final byte[] buffer1 = new byte[200];
        dao.readContent(d1, ByteBuffer.wrap(buffer1));
        final byte[] buffer2 = new byte[200];
        ByteStreams.readFully(ResourceUtil.getResourceAsStream("sample.bin"), buffer2);

        final byte[] oobBuffer1 = new byte[200];
        dao.readOobContent(d1, "ref", ByteBuffer.wrap(oobBuffer1));
        final byte[] oobBuffer2 = new byte[200];
        ByteStreams.readFully(ResourceUtil.getResourceAsStream("sample.xml"), oobBuffer2);

        Assert.assertArrayEquals(buffer1, buffer2);
        Assert.assertArrayEquals(oobBuffer1, oobBuffer2);
        final int d1Version = dao.getVersion(d1);
        dao.delete(d1, d1Version, new DoxPrincipal("PRINCE"));
        c.commit();
        c.close();
    }

    /**
     * When updating an OOB entry it will replace it in place. It won't keep the
     * history.
     *
     * @throws Exception
     */
    @Test
    public void testUpdateOob() throws Exception {

        final DoxConfiguration doxConfiguration = new DoxConfiguration();
        doxConfiguration.setTableName("sample");
        doxConfiguration.setHasOob(true);
        final JdbcDoxDAO dao = new JdbcDoxDAO(c, doxConfiguration);
        final DoxID d1 = dao.create(ResourceUtil.getResourceAsStream("sample.bin"), 1, new DoxPrincipal("PRINCE"));
        dao.create(ResourceUtil.getResourceAsStream("sample.bin"), 1, new DoxPrincipal("PRINCE"));
        final int version1 = dao.getVersion(d1);
        dao.attach(d1, "ref", ResourceUtil.getResourceAsStream("sample.bin"), version1, new DoxPrincipal("PRINSIPE"));

        final int version2 = dao.getVersion(d1);
        dao.attach(d1, "ref", ResourceUtil.getResourceAsStream("sample.xml"), version2, new DoxPrincipal("PRINSIPEUP"));

        assertTrue(version2 > version1);
        final byte[] buffer1 = new byte[200];
        dao.readOobContent(d1, "ref", ByteBuffer.wrap(buffer1));

        final byte[] buffer2 = new byte[200];
        ByteStreams.readFully(ResourceUtil.getResourceAsStream("sample.xml"), buffer2);
        assertArrayEquals(buffer1, buffer2);
        final int d1Version = dao.getVersion(d1);
        assertTrue(d1Version > version2);
        dao.delete(d1, d1Version, new DoxPrincipal("PRINCE"));
        c.commit();
    }

    /**
     * When updating an OOB entry it will replace it in place. It won't keep the
     * history.
     */
    @Test
    public void testUpdateOobSmallerLOB() throws Exception {

        final DoxConfiguration doxConfiguration = new DoxConfiguration();
        doxConfiguration.setTableName("sample");
        doxConfiguration.setHasOob(true);
        doxConfiguration.setOobLobSize(420000);
        final JdbcDoxDAO dao = new JdbcDoxDAO(c, doxConfiguration);
        final DoxID d1 = dao.create(ResourceUtil.getResourceAsStream("sample.bin"), 1, new DoxPrincipal("PRINCE"));
        dao.create(ResourceUtil.getResourceAsStream("sample.bin"), 1, new DoxPrincipal("PRINCE"));
        final int version1 = dao.getVersion(d1);
        dao.attach(d1, "ref", ResourceUtil.getResourceAsStream("sample.bin"), version1, new DoxPrincipal("PRINSIPE"));

        final int version2 = dao.getVersion(d1);
        dao.attach(d1, "ref", ResourceUtil.getResourceAsStream("sample.xml"), version2, new DoxPrincipal("PRINSIPEUP"));

        Assert.assertTrue(version2 > version1);
        final byte[] buffer1 = new byte[200];
        dao.readOobContent(d1, "ref", ByteBuffer.wrap(buffer1));

        final byte[] buffer2 = new byte[200];
        ByteStreams.readFully(ResourceUtil.getResourceAsStream("sample.xml"), buffer2);
        Assert.assertArrayEquals(buffer1, buffer2);
        final int d1Version = dao.getVersion(d1);
        Assert.assertTrue(d1Version > version2);
        dao.delete(d1, d1Version, new DoxPrincipal("PRINCE"));
        c.commit();
    }
}
