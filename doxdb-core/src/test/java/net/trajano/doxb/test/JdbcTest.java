package net.trajano.doxb.test;

import java.sql.Connection;
import java.sql.DriverManager;

import net.trajano.doxdb.DoxConfiguration;
import net.trajano.doxdb.DoxDAO;
import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxPrincipal;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;

public class JdbcTest {

    @Test
    public void testOobPersistence() throws Exception {

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        final Connection c = DriverManager.getConnection("jdbc:derby:memory:" + DoxID.generate() + ";create=true");

        final DoxConfiguration doxConfiguration = new DoxConfiguration();
        doxConfiguration.setTableName("sample");
        doxConfiguration.setHasOob(true);
        final DoxDAO dao = new DoxDAO(c, doxConfiguration);
        final DoxID d1 = dao.create(Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput(), new DoxPrincipal("PRINCE"));
        final DoxID d2 = dao.create(Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput(), new DoxPrincipal("PRINCE"));
        dao.attach(d1, "ref", Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput(), dao.getVersion(d1), new DoxPrincipal("PRINSIPE"));
        final byte[] buffer1 = new byte[5000];
        ByteStreams.readFully(dao.readContent(d1), buffer1);
        final byte[] buffer2 = new byte[5000];
        ByteStreams.readFully(Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput(), buffer2);
        Assert.assertArrayEquals(buffer1, buffer2);
        final int d1Version = dao.getVersion(d1);
        dao.delete(d1, d1Version, new DoxPrincipal("PRINCE"));
        c.commit();
        c.close();
    }

    // System.out.println(ID.generate());
    // System.out.println(ID.generate());
    // System.out.println(ID.generate());
    // System.out.println(ID.generate());
    // System.out.println(ID.generate());
    //
    // for (char i = 0x21; i < 0x7f; ++i) {
    // System.out.print(i);
    // }
    // // EntityManagerFactory emf =
    // // PersistenceProviderResolverHolder.getPersistenceProviderResolver()
    // // .getPersistenceProviders()
    // // .get(0)
    // // .createContainerEntityManagerFactory(new DoxdbPersistenceUnitInfo(),
    // // ImmutableMap.builder()
    // // .put("javax.persistence.jdbc.driver",
    // // "org.apache.derby.jdbc.EmbeddedDriver")
    // // .put("javax.persistence.schema-generation.database.action", "create")
    // // .put("javax.persistence.jdbc.url", "jdbc:derby:memory:" +
    // // UUID.randomUUID() + ";create=true")
    // // .put("eclipselink.logging.logger", "JavaLogger")
    // // .put("eclipselink.logging.level.sql", "fine")
    // // .put("eclipselink.logging.parameters", "true")
    // // .build());
    // //
    // // emf.createEntityManager();
    // // System.out.println(emf);
    // // emf.close();
    // }

    @Test
    public void testPersistence() throws Exception {

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        final Connection c = DriverManager.getConnection("jdbc:derby:memory:" + DoxID.generate() + ";create=true");

        // Connection c = DriverManager.getConnection("jdbc:derby://" +
        // InetAddress.getLocalHost()
        // .getHostName() +
        // ":1527/sun-appserv-samples;create=true;upgrade=true");

        final DoxDAO dao = new DoxDAO(c, "sample");
        final DoxID d1 = dao.create(Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput(), new DoxPrincipal("PRINCE"));
        final DoxID d2 = dao.create(Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput(), new DoxPrincipal("PRINCE"));
        System.out.println(d1);
        System.out.println(d2);
        final byte[] buffer1 = new byte[5000];
        ByteStreams.readFully(dao.readContent(d1), buffer1);
        final byte[] buffer2 = new byte[5000];
        ByteStreams.readFully(Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput(), buffer2);
        Assert.assertArrayEquals(buffer1, buffer2);
        final int d1Version = dao.getVersion(d1);
        dao.delete(d1, d1Version, new DoxPrincipal("PRINCE"));
        c.commit();
        c.close();
    }
}
