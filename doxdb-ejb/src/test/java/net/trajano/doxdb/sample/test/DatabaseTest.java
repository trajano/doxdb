package net.trajano.doxdb.sample.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;

import javax.ejb.SessionContext;
import javax.persistence.PersistenceException;

import org.bson.BsonDocument;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.trajano.doxdb.DoxMeta;
import net.trajano.doxdb.IndexView;
import net.trajano.doxdb.ejb.DoxBean;
import net.trajano.doxdb.ejb.Initializer;
import net.trajano.doxdb.ext.CollectionAccessControl;
import net.trajano.doxdb.ext.ConfigurationProvider;
import net.trajano.doxdb.ext.DefaultEventHandler;
import net.trajano.doxdb.ext.Indexer;
import net.trajano.doxdb.internal.DoxPrincipal;
import net.trajano.doxdb.schema.DoxPersistence;
import net.trajano.doxdb.schema.DoxType;
import net.trajano.doxdb.schema.SchemaType;

public class DatabaseTest {

    private DoxBean bean;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {

        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:file:" + testFolder.newFile().getAbsolutePath());
        ds.setUser("sa");
        ds.setPassword("sa");
        ds.setLogWriter(new PrintWriter(System.out));

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
        bean.setDataSource(ds);
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
        final BsonDocument bson = BsonDocument.parse(inputJson);
        final DoxMeta meta = bean.create("horse", bson);

        final BsonDocument readBson = BsonDocument.parse(bean.read("horse", meta.getDoxId())
            .getContentJson());

        assertEquals(bson.getString("name"), readBson.getString("name"));
    }

    @Test(expected = PersistenceException.class)
    public void testFailValidation() throws Exception {

        final String inputJson = "{\"noname\":\"abc\"}";
        final BsonDocument bson = BsonDocument.parse(inputJson);
        bean.create("horse", bson);

    }
}
