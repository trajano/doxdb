package net.trajano.doxb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.Callable;

import net.trajano.commons.testing.EqualsTestUtil;
import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxIDConverter;

import org.junit.Test;

public class IDTest {

    @Test
    public void testEqualsHashCodeToString() throws Exception {

        final DoxID generated = DoxID.generate();
        final DoxID rebuilt = new DoxID(generated.toString());
        assertEquals(generated, rebuilt);
        assertEquals(generated.hashCode(), rebuilt.hashCode());
    }

    @Test
    public void testEqualsHashCodeToStringUtility() throws Exception {

        final DoxID generated = DoxID.generate();
        EqualsTestUtil.assertEqualsImplementedCorrectly(new Callable<DoxID>() {
            @Override
            public DoxID call() throws Exception {
                return generated;
            }
        });
        EqualsTestUtil.assertEqualsImplementedCorrectly(new Callable<DoxID>() {
            @Override
            public DoxID call() throws Exception {
                return generated;
            }
        });
        final DoxID rebuilt = new DoxID(generated.toString());
        assertEquals(generated, rebuilt);
        assertEquals(generated.hashCode(), rebuilt.hashCode());
    }

    @Test
    public void testRandomness() throws Exception {

        final DoxID generated = DoxID.generate();
        assertFalse(generated.equals(DoxID.generate()));
        assertFalse(generated.equals(DoxID.generate()));
        assertFalse(generated.equals(DoxID.generate()));
        assertFalse(generated.equals(DoxID.generate()));
        assertFalse(generated.equals(DoxID.generate()));
    }

    @Test
    public void testWithConverterEqualsHashCodeToString() throws Exception {

        final DoxID generated = DoxID.generate();
        final DoxIDConverter doxIDConverter = new DoxIDConverter();
        final String dbval = doxIDConverter.convertToDatabaseColumn(generated);
        final DoxID rebuilt = doxIDConverter.convertToEntityAttribute(dbval);
        assertEquals(generated, rebuilt);
        assertEquals(generated.hashCode(), rebuilt.hashCode());
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
