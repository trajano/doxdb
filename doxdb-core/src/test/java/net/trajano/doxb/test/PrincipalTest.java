package net.trajano.doxb.test;

import static org.junit.Assert.assertEquals;
import net.trajano.doxdb.DoxPrincipal;
import net.trajano.doxdb.DoxPrincipalConverter;

import org.junit.Test;

public class PrincipalTest {

    @Test
    public void testEqualsHashCodeToString() throws Exception {

        final DoxPrincipal p1 = new DoxPrincipal("ARCH");
        final DoxPrincipal p2 = new DoxPrincipal("ARCH");
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testEqualsHashCodePrincipalConstructor() throws Exception {

        final DoxPrincipal p1 = new DoxPrincipal("ARCH");
        final DoxPrincipal p2 = new DoxPrincipal(p1);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testWithConverterEqualsHashCodeToString() throws Exception {

        final DoxPrincipal p1 = new DoxPrincipal("ARCH");
        final DoxPrincipalConverter converter = new DoxPrincipalConverter();
        final String dbval = converter.convertToDatabaseColumn(p1);
        final DoxPrincipal p2 = converter.convertToEntityAttribute(dbval);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    // @Test
    // public void testPersistence() throws Exception {
    //
    // Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
    // Connection c = DriverManager.getConnection("jdbc:derby:memory:" +
    // UUID.randomUUID() + ";create=true");
    // System.out.println(ID.generate());
    // System.out.println(ID.generate());
    // System.out.println(ID.generate());
    // System.out.println(ID.generate());
    // System.out.println(ID.generate());
    //
    // for (char i = 0x21; i < 0x7f; ++i) {
    // System.out.print(i);
    // }
    // c.close();
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
}
