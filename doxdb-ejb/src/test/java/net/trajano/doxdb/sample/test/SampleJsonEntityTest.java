package net.trajano.doxdb.sample.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.security.Principal;

import javax.ejb.SessionContext;
import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.bson.BsonDocument;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.trajano.doxdb.DoxMeta;
import net.trajano.doxdb.ejb.DoxBean;
import net.trajano.doxdb.ejb.Initializer;
import net.trajano.doxdb.internal.DoxPrincipal;
import net.trajano.doxdb.schema.DoxPersistence;
import net.trajano.doxdb.schema.DoxType;
import net.trajano.doxdb.schema.SchemaType;
import net.trajano.doxdb.search.IndexView;
import net.trajano.doxdb.spi.CollectionAccessControl;
import net.trajano.doxdb.spi.ConfigurationProvider;
import net.trajano.doxdb.spi.DefaultEventHandler;
import net.trajano.doxdb.spi.Indexer;

public class SampleJsonEntityTest {

    private DoxBean bean;

    private DataSource datasource;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {

        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:file:" + testFolder.newFile().getAbsolutePath());
        ds.setUser("sa");
        ds.setPassword("sa");
        ds.setLogWriter(new PrintWriter(System.out));
        datasource = ds;

        final ConfigurationProvider configurationProvider = new ConfigurationProvider() {

            @Override
            public DoxPersistence getPersistenceConfig() {

                final DoxPersistence doxPersistence = new DoxPersistence();
                final DoxType doxType = new DoxType();
                doxType.setName("horse");
                final SchemaType schema = new SchemaType();
                schema.setUri("/schema/horse.json");
                schema.setVersion(1);
                doxType.getSchema().add(schema);
                doxPersistence.getDox().add(doxType);
                return doxPersistence;
            }
        };

        final Initializer initializer = new Initializer();
        initializer.setDataSource(ds);
        initializer.setConfigurationProvider(configurationProvider);
        initializer.init();

        bean = new DoxBean();
        bean.setDataSource(datasource);
        final SessionContext sessionContextMock = mock(SessionContext.class);
        when(sessionContextMock.getCallerPrincipal()).thenReturn(new DoxPrincipal("ANONYMOUS"));
        bean.setSessionContext(sessionContextMock);
        bean.setIndexer(new Indexer() {

            @Override
            public IndexView[] buildIndexViews(final String collection,
                final String json) {

                return new IndexView[0];
            }
        });
        bean.setCollectionAccessControl(new CollectionAccessControl() {

            @Override
            public byte[] buildAccessKey(final String collection,
                final String json,
                final Principal principal) {

                return null;
            }
        });
        bean.setEventHandler(new DefaultEventHandler());
        bean.setConfigurationProvider(configurationProvider);
        bean.init();
    }

    @Test
    public void testCrud() throws Exception {

        final String inputJson = "{\"name\":\"abc\"}";
        final JsonObject o = Json.createReader(new StringReader(inputJson))
            .readObject();
        final DoxMeta meta = bean.create("horse", BsonDocument.parse(inputJson));
        Assert.assertEquals(inputJson, bean.read("horse", meta.getDoxId())
            .getContentJson());
    }

    @Ignore
    @Test(expected = PersistenceException.class)
    public void testFailValidation() throws Exception {

        final String inputJson = "{\"noname\":\"abc\"}";
        final JsonObject o = Json.createReader(new StringReader(inputJson))
            .readObject();
        //        final DoxID id = bean.create(o, new DoxPrincipal("PRINCE"));
        //        Assert.assertEquals(inputJson, bean.readContent(id)
        //            .toString());

    }
}
