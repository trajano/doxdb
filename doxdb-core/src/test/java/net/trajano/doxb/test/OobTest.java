package net.trajano.doxb.test;

import java.sql.Connection;
import java.sql.DriverManager;

import javax.persistence.EntityNotFoundException;

import net.trajano.doxdb.DoxConfiguration;
import net.trajano.doxdb.DoxDAO;
import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxPrincipal;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;

public class OobTest {

    static byte[] sample1;

    static byte[] sample2;

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
        final DoxDAO dao = new DoxDAO(c, doxConfiguration);

        final DoxID d1 = dao.create(Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput(), new DoxPrincipal("PRINCE"));
        dao.create(Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput(), new DoxPrincipal("PRINCE"));

        try {
            dao.readOobContent(d1, "ref");
            Assert.fail();
        } catch (final EntityNotFoundException e) {
            // Expected
        }

        final int version1 = dao.getVersion(d1);
        dao.attach(d1, "ref", Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput(), version1, new DoxPrincipal("PRINSIPE"));

        Assert.assertNotNull(dao.readOobContent(d1, "ref"));

        final int version2 = dao.getVersion(d1);
        dao.detach(d1, "ref", version2, new DoxPrincipal("PRINSIPEUP"));

        Assert.assertTrue(version2 > version1);

        try {
            dao.readOobContent(d1, "ref");
            Assert.fail();
        } catch (final EntityNotFoundException e) {
            // Expected
        }
        dao.readContent(d1);
        c.commit();
    }

    @Test
    public void testOobPersistence() throws Exception {

        final DoxConfiguration doxConfiguration = new DoxConfiguration();
        doxConfiguration.setTableName("sample");
        doxConfiguration.setHasOob(true);
        final DoxDAO dao = new DoxDAO(c, doxConfiguration);
        final DoxID d1 = dao.create(Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput(), new DoxPrincipal("PRINCE"));
        dao.create(Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput(), new DoxPrincipal("PRINCE"));
        dao.attach(d1, "ref", Resources.newInputStreamSupplier(Resources.getResource("sample.xml"))
                .getInput(), dao.getVersion(d1), new DoxPrincipal("PRINSIPE"));

        final byte[] buffer1 = new byte[200];
        ByteStreams.readFully(dao.readContent(d1), buffer1);
        final byte[] buffer2 = new byte[200];
        ByteStreams.readFully(Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput(), buffer2);

        final byte[] oobBuffer1 = new byte[200];
        ByteStreams.readFully(dao.readOobContent(d1, "ref"), oobBuffer1);
        final byte[] oobBuffer2 = new byte[200];
        ByteStreams.readFully(Resources.newInputStreamSupplier(Resources.getResource("sample.xml"))
                .getInput(), oobBuffer2);

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
        final DoxDAO dao = new DoxDAO(c, doxConfiguration);
        final DoxID d1 = dao.create(Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput(), new DoxPrincipal("PRINCE"));
        dao.create(Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput(), new DoxPrincipal("PRINCE"));
        final int version1 = dao.getVersion(d1);
        dao.attach(d1, "ref", Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput(), version1, new DoxPrincipal("PRINSIPE"));

        final int version2 = dao.getVersion(d1);
        dao.attach(d1, "ref", Resources.newInputStreamSupplier(Resources.getResource("sample.xml"))
                .getInput(), version2, new DoxPrincipal("PRINSIPEUP"));

        Assert.assertTrue(version2 > version1);
        final byte[] buffer1 = new byte[200];
        ByteStreams.readFully(dao.readOobContent(d1, "ref"), buffer1);

        final byte[] buffer2 = new byte[200];
        ByteStreams.readFully(Resources.newInputStreamSupplier(Resources.getResource("sample.xml"))
                .getInput(), buffer2);
        Assert.assertArrayEquals(buffer1, buffer2);
        final int d1Version = dao.getVersion(d1);
        Assert.assertTrue(d1Version > version2);
        dao.delete(d1, d1Version, new DoxPrincipal("PRINCE"));
        c.commit();
    }
}
