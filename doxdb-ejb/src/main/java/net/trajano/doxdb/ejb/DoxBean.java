package net.trajano.doxdb.ejb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.bson.BsonArray;
import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
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

import net.trajano.doxdb.Dox;
import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxMeta;
import net.trajano.doxdb.IndexView;
import net.trajano.doxdb.SearchResult;
import net.trajano.doxdb.ejb.internal.DoxSearch;
import net.trajano.doxdb.ejb.internal.SqlConstants;
import net.trajano.doxdb.ext.CollectionAccessControl;
import net.trajano.doxdb.ext.ConfigurationProvider;
import net.trajano.doxdb.ext.EventHandler;
import net.trajano.doxdb.ext.Indexer;
import net.trajano.doxdb.ext.Migrator;
import net.trajano.doxdb.internal.DoxPrincipal;
import net.trajano.doxdb.schema.DoxPersistence;
import net.trajano.doxdb.schema.DoxType;
import net.trajano.doxdb.schema.SchemaType;

/**
 * This will be an SLSB. There should be many instances of this and should be
 * able to spread through the EJB pool.
 *
 * @author Archimedes Trajano
 */
@Stateless
@Remote(Dox.class)
public class DoxBean implements
    Dox {

    private CollectionAccessControl collectionAccessControl;

    private ConfigurationProvider configurationProvider;

    private SessionContext ctx;

    private transient Map<String, SchemaType> currentSchemaMap = new HashMap<>();

    private transient Map<String, DoxType> doxen = new HashMap<>();

    private DoxSearch doxSearchBean;

    private DataSource ds;

    private EventHandler eventHandler;

    private Indexer indexer;

    private transient ConcurrentMap<String, JsonSchema> jsonSchemaMap = new ConcurrentHashMap<>();

    private Migrator migrator;

    private transient DoxPersistence persistenceConfig;

    @Override
    public DoxMeta create(final String collectionName,
        final BsonDocument bson) {

        final DoxType config = doxen.get(collectionName);
        final SchemaType schema = currentSchemaMap.get(collectionName);

        final String inputJson = bson.toJson();
        validate(schema, inputJson);
        try (Connection c = ds.getConnection()) {
            final DoxID doxId = DoxID.generate();

            final BasicOutputBuffer basicOutputBuffer = new BasicOutputBuffer();

            bson.put("_id", new BsonString(doxId.toString()));
            bson.put("_version", new BsonInt32(1));
            new BsonDocumentCodec().encode(new BsonBinaryWriter(basicOutputBuffer), bson, EncoderContext.builder()
                .build());

            final String storedJson = bson.toJson();
            final byte[] accessKey = collectionAccessControl.buildAccessKey(config.getName(), storedJson, ctx.getCallerPrincipal());

            try (final PreparedStatement s = c.prepareStatement(String.format(SqlConstants.INSERT, config.getName().toUpperCase()), Statement.RETURN_GENERATED_KEYS)) {

                final Timestamp ts = new Timestamp(System.currentTimeMillis());
                s.setBytes(1, basicOutputBuffer.toByteArray());
                s.setString(2, doxId.toString());
                s.setString(3, ctx.getCallerPrincipal().getName());
                s.setTimestamp(4, ts);
                s.setString(5, ctx.getCallerPrincipal().getName());
                s.setTimestamp(6, ts);
                s.setInt(7, 1);
                s.setInt(8, schema.getVersion());
                s.setBytes(9, accessKey);
                s.executeUpdate();
                try (final ResultSet rs = s.getGeneratedKeys()) {
                    rs.next();

                    final IndexView[] indexViews = indexer.buildIndexViews(config.getName(), inputJson);
                    for (final IndexView indexView : indexViews) {
                        indexView.setCollection(config.getName());
                        indexView.setDoxID(doxId);
                    }
                    if (indexViews.length > 0) {
                        doxSearchBean.addToIndex(indexViews);
                    }
                    final DoxMeta meta = new DoxMeta();
                    meta.setAccessKey(accessKey);
                    meta.setLastUpdatedOn(ts);
                    meta.setVersion(1);
                    meta.setDoxId(doxId);
                    meta.setContentJson(storedJson);

                    eventHandler.onRecordCreate(config.getName(), doxId, storedJson);
                    return meta;
                }
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void delete(final String collection,
        final DoxID doxid,
        final int version) {

        final Timestamp ts = new Timestamp(System.currentTimeMillis());
        final DoxType config = doxen.get(collection);

        try (Connection c = ds.getConnection()) {
            final DoxMeta meta = readMetaAndLock(c, config.getName(), config.isOob(), doxid, version);
            if (config.isOob()) {
                //                deleteOob(ctx.getCallerPrincipal(), meta, ts);
            }

            meta.getAccessKey();
            // TODO check the security.

            try (final PreparedStatement s = c.prepareStatement(String.format(SqlConstants.COPYTOTOMBSTONESQL, config.getName().toUpperCase()))) {
                s.setString(1, ctx.getCallerPrincipal().getName());
                s.setTimestamp(2, ts);
                s.setLong(3, meta.getId());
                s.setInt(4, meta.getVersion());
                s.executeUpdate();
            }
            try (final PreparedStatement t = c.prepareStatement(String.format(SqlConstants.DELETE, config.getName().toUpperCase()))) {
                t.setLong(1, meta.getId());
                t.setInt(2, meta.getVersion());
                final int deletedRows = t.executeUpdate();
                if (deletedRows != 1) {
                    throw new PersistenceException("problem with the delete");
                }
            }
            doxSearchBean.removeFromIndex(collection, doxid);
            eventHandler.onRecordDelete(config.getName(), doxid);
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }

    }

    /**
     * This will initialize the EJB and prepare the statements used by the
     * framework. It will create the tables as needed.
     */
    @PostConstruct
    public void init() {

        persistenceConfig = configurationProvider.getPersistenceConfig();

        for (final DoxType doxConfig : persistenceConfig.getDox()) {
            doxen.put(doxConfig.getName(), doxConfig);
            currentSchemaMap.put(doxConfig.getName(), doxConfig.getSchema().get(doxConfig.getSchema().size() - 1));
        }
    }

    @Override
    public void noop() {

    }

    @Override
    public DoxMeta read(final String collectionName,
        final DoxID doxid) {

        final DoxType config = doxen.get(collectionName);
        final SchemaType schema = currentSchemaMap.get(collectionName);

        try (Connection c = ds.getConnection()) {
            final DoxMeta meta = readMeta(c, config.getName(), doxid);

            meta.getAccessKey();
            // TODO check the security.

            final BsonDocument document = readContent(c, config.getName(), meta.getId());

            final String json = document.toJson();
            if (meta.getContentVersion() != schema.getVersion()) {
                migrator.migrate(collectionName, meta.getContentVersion(), schema.getVersion(), json);
            }

            meta.setContentJson(json);
            eventHandler.onRecordCreate(config.getName(), doxid, json);
            return meta;

        } catch (final EntityNotFoundException e) {
            return null;
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public BsonArray readAll(final String collectionName) {

        final DoxType config = doxen.get(collectionName);
        if (!config.isReadAll()) {
            throw new PersistenceException("Not supported");
        }
        final SchemaType schema = currentSchemaMap.get(collectionName);

        final BsonArray all = new BsonArray();
        try (Connection c = ds.getConnection()) {
            final String sql = String.format(SqlConstants.READALLCONTENT, config.getName().toUpperCase());
            try (final Statement s = c.createStatement()) {
                try (final ResultSet rs = s.executeQuery(sql)) {
                    while (rs.next()) {

                        final Blob blob = rs.getBlob(2);
                        // final byte[] accessKey = rs.getBytes(3);
                        final int contentVersion = rs.getInt(5);
                        final BsonDocument decoded = new BsonDocumentCodec().decode(new BsonBinaryReader(ByteBuffer.wrap(blob.getBytes(1, (int) blob.length()))), DecoderContext.builder()
                            .build());
                        blob.free();

                        if (contentVersion != schema.getVersion()) {
                            migrator.migrate(config.getName(), contentVersion, schema.getVersion(), decoded.toJson());
                            // queue migrate later?
                        }

                        all.add(decoded);

                    }
                }
            }

            return all;
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

    private DoxMeta readMeta(final Connection c,
        final String collectionName,
        final DoxID id) {

        try (final PreparedStatement s = c.prepareStatement(String.format(SqlConstants.READ, collectionName.toUpperCase()))) {
            s.setString(1, id.toString());
            try (final ResultSet rs = s.executeQuery()) {
                if (!rs.next()) {
                    throw new EntityNotFoundException();
                }
                final DoxMeta meta = new DoxMeta();
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

    private DoxMeta readMetaAndLock(final Connection c,
        final String collectionName,
        final boolean hasOob,
        final DoxID id,
        final int version) {

        try (final PreparedStatement s = c.prepareStatement(String.format(SqlConstants.READFORUPDATE, collectionName.toUpperCase()))) {
            s.setString(1, id.toString());
            s.setInt(2, version);
            try (final ResultSet rs = s.executeQuery()) {
                if (!rs.next()) {
                    throw new OptimisticLockException();
                }
                final DoxMeta meta = new DoxMeta();
                meta.setId(rs.getLong(1));
                meta.setDoxId(new DoxID(rs.getString(2)));
                meta.setCreatedBy(new DoxPrincipal(rs.getString(3)));
                meta.setCreatedOn(rs.getTimestamp(4));
                meta.setLastUpdatedBy(new DoxPrincipal(rs.getString(5)));
                meta.setLastUpdatedOn(rs.getTimestamp(6));
                meta.setVersion(rs.getInt(7));
                meta.setContentVersion(rs.getInt(8));
                meta.setAccessKey(rs.getBytes(9));

                if (hasOob) {
                    try (final PreparedStatement os = c.prepareStatement(String.format(SqlConstants.OOBREADFORUPDATE, collectionName.toUpperCase()))) {
                        os.setLong(1, meta.getId());
                        os.executeQuery()
                            .close();
                    }
                }

                return meta;
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    @Asynchronous
    public void reindex() {

        doxSearchBean.reset();

        try (final Connection c = ds.getConnection()) {

            for (final DoxType config : doxen.values()) {

                final String sql = String.format(SqlConstants.READALLCONTENT, config.getName().toUpperCase());
                try (final Statement s = c.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {
                    final List<IndexView> indexViews = new LinkedList<>();
                    try (final ResultSet rs = s.executeQuery(sql)) {
                        while (rs.next()) {

                            final DoxID doxid = new DoxID(rs.getString(1));
                            final Blob blob = rs.getBlob(2);

                            final BsonDocument decoded = new BsonDocumentCodec().decode(new BsonBinaryReader(ByteBuffer.wrap(blob.getBytes(1, (int) blob.length()))), DecoderContext.builder()
                                .build());
                            blob.free();
                            decoded.remove("_id");
                            decoded.remove("_version");
                            final String json = decoded.toJson();
                            rs.updateBytes(3, collectionAccessControl.buildAccessKey(config.getName(), json, new DoxPrincipal(rs.getString(4))));
                            final IndexView[] indexViewBuilt = indexer.buildIndexViews(config.getName(), json);
                            for (final IndexView indexView : indexViewBuilt) {
                                indexView.setCollection(config.getName());
                                indexView.setDoxID(doxid);
                                indexViews.add(indexView);
                            }

                        }
                    }
                    doxSearchBean.addToIndex(indexViews.toArray(new IndexView[0]));
                }

            }

        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public SearchResult search(final String index,
        final String queryString,
        final int limit) {

        return doxSearchBean.search(index, queryString, limit);
    }

    @EJB
    public void setCollectionAccessControl(final CollectionAccessControl collectionAccessControl) {

        this.collectionAccessControl = collectionAccessControl;
    }

    @EJB
    public void setConfigurationProvider(final ConfigurationProvider configurationProvider) {

        this.configurationProvider = configurationProvider;
    }

    @Resource
    public void setDataSource(final DataSource ds) {

        this.ds = ds;
    }

    @EJB
    public void setDoxSearchBean(final DoxSearch doxSearchBean) {

        this.doxSearchBean = doxSearchBean;
    }

    @EJB
    public void setEventHandler(final EventHandler eventHandler) {

        this.eventHandler = eventHandler;
    }

    @EJB
    public void setIndexer(final Indexer indexer) {

        this.indexer = indexer;
    }

    @EJB
    public void setMigrator(final Migrator migrator) {

        this.migrator = migrator;
    }

    @Resource
    public void setSessionContext(final SessionContext ctx) {

        this.ctx = ctx;
    }

    @Override
    public DoxMeta update(final String collectionName,
        final DoxID doxid,
        final BsonDocument bson,
        final int version) {

        final Timestamp ts = new Timestamp(System.currentTimeMillis());
        final DoxType config = doxen.get(collectionName);
        final SchemaType schema = currentSchemaMap.get(collectionName);

        bson.remove("_id");
        bson.remove("_version");
        validate(schema, bson.toJson());
        try (Connection c = ds.getConnection()) {
            final DoxMeta meta = readMetaAndLock(c, config.getName(), config.isOob(), doxid, version);

            meta.getAccessKey();
            // TODO check the security.

            final BasicOutputBuffer basicOutputBuffer = new BasicOutputBuffer();

            final IndexView[] indexViews = indexer.buildIndexViews(config.getName(), bson.toJson());

            bson.put("_id", new BsonString(doxid.toString()));
            bson.put("_version", new BsonInt32(version + 1));
            new BsonDocumentCodec().encode(new BsonBinaryWriter(basicOutputBuffer), bson, EncoderContext.builder()
                .build());

            final String storedJson = bson.toJson();
            final byte[] accessKey = collectionAccessControl.buildAccessKey(config.getName(), storedJson, ctx.getCallerPrincipal());

            try (final PreparedStatement s = c.prepareStatement(String.format(SqlConstants.UPDATE, config.getName().toUpperCase()), Statement.RETURN_GENERATED_KEYS)) {

                s.setBytes(1, basicOutputBuffer.toByteArray());
                s.setString(2, ctx.getCallerPrincipal().getName());
                s.setTimestamp(3, ts);
                s.setInt(4, schema.getVersion());
                s.setBytes(5, accessKey);

                s.setLong(6, meta.getId());
                s.setInt(7, version);
                final int count = s.executeUpdate();
                if (count != 1) {
                    throw new PersistenceException("Update failed");
                }

                for (final IndexView indexView : indexViews) {
                    indexView.setCollection(config.getName());
                    indexView.setDoxID(doxid);
                }
                doxSearchBean.addToIndex(indexViews);

            }
            meta.setAccessKey(accessKey);
            meta.setVersion(version + 1);
            meta.setContentJson(storedJson);
            eventHandler.onRecordUpdate(config.getName(), doxid, storedJson);
            return meta;
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }

    }

    private void validate(final SchemaType schema,
        final String json) {

        try {

            JsonSchema jsonSchema = jsonSchemaMap.get(schema.getUri());
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

            final ProcessingReport validate = jsonSchema.validate(JsonLoader.fromString(json));
            if (!validate.isSuccess()) {
                throw new PersistenceException(validate.toString());
            }
        } catch (ProcessingException
            | IOException e) {
            throw new PersistenceException(e);
        }
    }
}
