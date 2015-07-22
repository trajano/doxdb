package net.trajano.doxdb.jdbc;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import net.trajano.doxdb.DoxFactory;
import net.trajano.doxdb.schema.DoxPersistence;

/**
 * Like an Entity Manager Factory, this is a singleton in relation to an
 * application. It is expected that an EJB jar will load this through the
 * ejb-jar.xml file. The DoxFactory will create the necessary tables.
 *
 * @author Archimedes
 */
public class JdbcDoxFactory implements
    DoxFactory {

    private DoxPersistence persistenceConfig;

    public JdbcDoxFactory() {

    }

    @Override
    public void close() {

    }

    @PostConstruct
    public void init() {

        System.out.println("init from factory");
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/dox.xml")) {
            final JAXBContext jaxb = JAXBContext.newInstance(DoxPersistence.class);
            final Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            unmarshaller.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(getClass().getResource("/META-INF/xsd/dox.xsd")));
            persistenceConfig = (DoxPersistence) unmarshaller.unmarshal(is);
        } catch (final IOException
            | SAXException
            | JAXBException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
