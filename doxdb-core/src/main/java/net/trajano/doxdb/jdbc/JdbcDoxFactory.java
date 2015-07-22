package net.trajano.doxdb.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import net.trajano.doxdb.Dox;
import net.trajano.doxdb.DoxFactory;
import net.trajano.doxdb.JsonDox;
import net.trajano.doxdb.schema.DoxPersistence;
import net.trajano.doxdb.schema.DoxType;

/**
 * Like an Entity Manager Factory, this is a singleton in relation to an
 * application. It is expected that an EJB jar will load this through the
 * ejb-jar.xml file. The DoxFactory will create the necessary tables.
 * <p>
 * This won't scale properly because everything goes to this one instance. What
 * we need is a way of doing all the operations through an EJB, in theory only
 * one SLSB EJB is needed.
 * <p>
 * However, an Singleton bean is still needed to initialize the SLSB.
 *
 * @author Archimedes
 */
public class JdbcDoxFactory implements
    DoxFactory {

    private final ConcurrentMap<String, Dox> doxen = new ConcurrentHashMap<>();

    /**
     * The data source. It is required that the datasource be XA enabled so it
     * can co-exist with JPA and other operations.
     */
    @Resource
    private DataSource ds;

    public JdbcDoxFactory() {

    }

    @Override
    public void close() {

    }

    public Dox getDox(final String name) {

        return doxen.get(name);
    }

    @PostConstruct
    public void init() {

        System.out.println("datasource = " + ds);
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/dox.xml")) {
            final JAXBContext jaxb = JAXBContext.newInstance(DoxPersistence.class);
            final Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            unmarshaller.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(getClass().getResource("/META-INF/xsd/dox.xsd")));
            final DoxPersistence persistenceConfig = (DoxPersistence) unmarshaller.unmarshal(is);

            // at this point do the table creation and other fun stuff

            for (final DoxType doxConfig : persistenceConfig.getDox()) {
                doxen.put(doxConfig.getName(), new JsonDox(ds, doxConfig));
            }

        } catch (final IOException
            | SAXException
            | JAXBException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
