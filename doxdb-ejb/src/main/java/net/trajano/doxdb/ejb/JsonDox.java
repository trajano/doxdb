package net.trajano.doxdb.ejb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;

import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import net.trajano.doxdb.CollectionAccessControl;
import net.trajano.doxdb.ConfigurationProvider;
import net.trajano.doxdb.Dox;
import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxSearch;
import net.trajano.doxdb.Indexer;
import net.trajano.doxdb.ejb.internal.SqlConstants;
import net.trajano.doxdb.jdbc.DocumentMeta;
import net.trajano.doxdb.jdbc.DoxPrincipal;
import net.trajano.doxdb.schema.DoxPersistence;
import net.trajano.doxdb.schema.DoxType;
import net.trajano.doxdb.schema.SchemaType;
import net.trajano.doxdb.search.IndexView;

/**
 * This will be an SLSB. There should be many instances of this and should be
 * able to spread through the EJB pool.
 *
 * @author trajanar
 */
@Stateless
@Remote(Dox.class)
public class JsonDox implements
    Dox {

    @EJB
    private CollectionAccessControl collectionAccessControl;

    @EJB
    private ConfigurationProvider configurationProvider;

    @Resource
    private SessionContext ctx;

    private transient Map<String, SchemaType> currentSchemaMap = new HashMap<>();

    private transient Map<String, DoxType> doxen = new HashMap<>();

    @EJB
    private DoxSearch doxSearchBean;

    @Resource
    private DataSource ds;

    @EJB
    private Indexer indexer;

    /**
     * Time the initialization happened.
     */
    private transient long initTimeInMillis;

    private transient ConcurrentMap<String, JsonSchema> jsonSchemaMap = new ConcurrentHashMap<>();

    private transient Properties oobSqls = new Properties();

    private transient DoxPersistence persistenceConfig;

    private transient Properties sqls = new Properties();

    /**
     * Logs the destruction of the EJB. This is used to determine if the pool
     * size may need to be increased or not. In Wildfly, session bean pooling is
     * disabled by default and will incur significant performance loss.
     */
    @PreDestroy
    public void cleanup() {

        System.out.println("Uninitializing dox " + this);
        if (System.currentTimeMillis() - initTimeInMillis < 10 * 1000) {
            System.out.println("EJB " + this + " was deallocated in less than 10 seconds, check the stateless session pool size value.");
        }

    }

    @Override
    public String create(final String collectionName,
        final String json) {

        final DoxType config = doxen.get(collectionName);
        final SchemaType schema = currentSchemaMap.get(collectionName);

        JsonSchema jsonSchema;
        try {
            jsonSchema = jsonSchemaMap.get(schema.getUri());
            if (jsonSchema == null) {
                final ValidationConfiguration cfg = ValidationConfiguration.newBuilder()
                    .setDefaultVersion(SchemaVersion.DRAFTV4)
                    .freeze();

                jsonSchema = JsonSchemaFactory.newBuilder()
                    .setValidationConfiguration(cfg)
                    .freeze()
                    .getJsonSchema(JsonLoader.fromResource(schema.getUri()));
                jsonSchemaMap.putIfAbsent(schema.getUri(), jsonSchema);
            }
        } catch (ProcessingException
            | IOException e) {
            throw new PersistenceException(e);
        }
        validate(jsonSchema, json);
        try (Connection c = ds.getConnection()) {
            final DoxID doxId = DoxID.generate();

            final BasicOutputBuffer basicOutputBuffer = new BasicOutputBuffer();

            final BsonDocument document = BsonDocument.parse(json);
            document.put("_id", new BsonString(doxId.toString()));
            new BsonDocumentCodec().encode(new BsonBinaryWriter(basicOutputBuffer), document, EncoderContext.builder()
                .build());

            final String storedJson = document.toJson();
            try (final InputStream in = new ByteArrayInputStream(basicOutputBuffer.toByteArray())) {

                try (final PreparedStatement s = c.prepareStatement(String.format(SqlConstants.INSERT, config.getName().toUpperCase()), Statement.RETURN_GENERATED_KEYS)) {

                    final Timestamp ts = new Timestamp(System.currentTimeMillis());
                    s.setBinaryStream(1, in);
                    s.setString(2, doxId.toString());
                    s.setString(3, ctx.getCallerPrincipal().getName());
                    s.setTimestamp(4, ts);
                    s.setString(5, ctx.getCallerPrincipal().getName());
                    s.setTimestamp(6, ts);
                    s.setInt(7, 1);
                    s.setInt(8, schema.getVersion().intValue());
                    s.setBytes(9, collectionAccessControl.buildAccessKeyForCreate(config.getName(), storedJson, ctx.getCallerPrincipal()));
                    s.executeUpdate();
                    try (final ResultSet rs = s.getGeneratedKeys()) {
                        rs.next();

                        final IndexView indexView = indexer.buildIndexView(config.getName(), storedJson);
                        doxSearchBean.addToIndex(indexView.getIndex(), config.getName(), doxId, indexView);

                        return storedJson;
                    }
                }
            }
        } catch (final IOException
            | SQLException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * This will initialize the EJB and prepare the statements used by the
     * framework. It will create the tables as needed.
     */
    @PostConstruct
    public void init() {

        try (InputStream is = getClass().getResourceAsStream("/META-INF/sqls.properties")) {
            sqls.load(is);
        } catch (final IOException e) {
            throw new PersistenceException(e);
        }
        try (InputStream is = getClass().getResourceAsStream("/META-INF/oob-sqls.properties")) {
            oobSqls.load(is);
        } catch (final IOException e) {
            throw new PersistenceException(e);
        }
        persistenceConfig = configurationProvider.getPersistenceConfig();

        try (final Connection c = ds.getConnection()) {

            for (final DoxType doxConfig : persistenceConfig.getDox()) {
                for (final Object sql : sqls.values()) {
                    final PreparedStatement stmt = c.prepareStatement(String.format(sql.toString(), doxConfig.getName().toUpperCase()));
                    stmt.close();
                }
                if (doxConfig.isOob()) {
                    for (final Object sql : oobSqls.values()) {
                        final PreparedStatement stmt = c.prepareStatement(String.format(sql.toString(), doxConfig.getName().toUpperCase()));
                        stmt.close();
                    }
                }
                doxen.put(doxConfig.getName(), doxConfig);
                currentSchemaMap.put(doxConfig.getName(), doxConfig.getSchema().get(doxConfig.getSchema().size() - 1));
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
        initTimeInMillis = System.currentTimeMillis();
    }

    @Override
    public void noop() {

    }

    @Override
    public String read(final String collectionName,
        final DoxID id) {

        final DoxType config = doxen.get(collectionName);
        final SchemaType schema = currentSchemaMap.get(collectionName);

        try (Connection c = ds.getConnection()) {

            final DocumentMeta meta = readMeta(c, config.getName(), id);

            if (meta.getContentVersion() != schema.getVersion()) {
                // TODO migrate data .
            }

            meta.getAccessKey();
            // TODO check the security.
            final BsonDocument document = readContent(c, config.getName(), meta.getId());

            return document.toJson();

        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    private BsonDocument readContent(final Connection c,
        final String collectionName,
        final long id) {

        try (final PreparedStatement s = c.prepareStatement(String.format(SqlConstants.READCONTENT, collectionName.toUpperCase()))) {
            s.setLong(1, id);
            try (final ResultSet rs = s.executeQuery()) {
                if (!rs.next()) {
                    throw new EntityNotFoundException();
                }

                final Blob blob = rs.getBlob(1);

                final BsonDocument decoded = new BsonDocumentCodec().decode(new BsonBinaryReader(ByteBuffer.wrap(blob.getBytes(1, (int) blob.length()))), DecoderContext.builder()
                    .build());
                blob.free();
                return decoded;
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }

    }

    private DocumentMeta readMeta(final Connection c,
        final String collectionName,
        final DoxID id) {

        try (final PreparedStatement s = c.prepareStatement(String.format(SqlConstants.READ, collectionName.toUpperCase()))) {
            s.setString(1, id.toString());
            try (final ResultSet rs = s.executeQuery()) {
                if (!rs.next()) {
                    throw new EntityNotFoundException();
                }
                final DocumentMeta meta = new DocumentMeta();
                meta.setId(rs.getLong(1));
                meta.setDoxId(new DoxID(rs.getString(2)));
                meta.setCreatedBy(new DoxPrincipal(rs.getString(3)));
                meta.setCreatedOn(rs.getTimestamp(4));
                meta.setLastUpdatedBy(new DoxPrincipal(rs.getString(5)));
                meta.setLastUpdatedOn(rs.getTimestamp(6));
                meta.setVersion(rs.getInt(7));
                meta.setContentVersion(rs.getInt(8));
                meta.setAccessKey(rs.getBytes(9));
                return meta;
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    private void validate(final JsonSchema schema,
        final String json) {

        try {
            final ProcessingReport validate = schema.validate(JsonLoader.fromString(json));
            if (!validate.isSuccess()) {
                throw new PersistenceException(validate.toString());
            }
        } catch (ProcessingException
            | IOException e) {
            throw new PersistenceException(e);
        }
    }
}
