package net.trajano.doxdb.jdbc;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import net.trajano.doxdb.ConfigurationProvider;
import net.trajano.doxdb.schema.DoxPersistence;

/**
 * <p>
 * This loads up the configuration from an XML file. The file is called
 * <code>META-INF/dox.xml</code>. This class does not get loaded automatically
 * as an EJB, instead it is expected that an EJB JAR file will declare this in
 * the <code>META-INF/ejb-jar.xml</code> file to allow custom providers specific
 * to the application if required. It is configured as follows:
 * </p>
 *
 * <pre>
 * &lt;enterprise-beans>
 *   &lt;session>
 *     &lt;ejb-name>XmlConfigurationProvider&lt;/ejb-name>
 *     &lt;business-local>net.trajano.doxdb.ConfigurationProvider&lt;/business-local>
 *     &lt;ejb-class>net.trajano.doxdb.jdbc.XmlConfigurationProvider&lt;/ejb-class>
 *     &lt;session-type>Stateless&lt;/session-type>
 *   &lt;/session>
 * &lt;/enterprise-beans>
 * </pre>
 *
 * @author Archimedes Trajano
 */
public class XmlConfigurationProvider implements
    ConfigurationProvider {

    private final DoxPersistence persistenceConfig;

    public XmlConfigurationProvider() {

        try (final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/dox.xml")) {
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

    @Override
    public DoxPersistence getPersistenceConfig() {

        return persistenceConfig;
    }

}
