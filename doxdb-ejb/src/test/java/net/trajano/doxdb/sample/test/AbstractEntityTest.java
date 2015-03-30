package net.trajano.doxdb.sample.test;

import java.util.UUID;
import java.util.logging.LogManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;

public class AbstractEntityTest {
    @BeforeClass
    public static void setupLogger() throws Exception {
        LogManager.getLogManager()
        .readConfiguration(Resources.getResource("logging.properties")
                .openStream());
    }

    protected EntityManager em;
    protected EntityManagerFactory emf;

    protected EntityTransaction tx;

    @Before
    public void setupEntityManager() {
        emf = Persistence.createEntityManagerFactory("default", ImmutableMap.builder()
                .put("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver")
                .put("javax.persistence.schema-generation.database.action", "create")
                .put("javax.persistence.jdbc.url", "jdbc:derby:memory:" + UUID.randomUUID() + ";create=true")
                .put("eclipselink.logging.logger", "JavaLogger")
                .put("eclipselink.logging.level.sql", "fine")
                .put("eclipselink.logging.parameters", "true")
                .build());
        em = emf.createEntityManager();
        tx = em.getTransaction();

    }

    @After
    public void tearDownEntityManager() {
        em.close();
        emf.close();
    }

}
