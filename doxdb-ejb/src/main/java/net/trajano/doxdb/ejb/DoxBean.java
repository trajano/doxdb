package net.trajano.doxdb.ejb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
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
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.bson.BsonArray;
import org.bson.BsonBinaryReader;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;

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

    @PersistenceContext
    private EntityManager em;

    private EventHandler eventHandler;

    private Indexer indexer;

    private transient ConcurrentMap<String, JsonSchema> jsonSchemaMap = new ConcurrentHashMap<>();

    private Migrator migrator;

    private transient DoxPersistence persistenceConfig;

    @Override
    public DoxMeta create(final String schemaName,
        final BsonDocument bson) {

        final Date ts = new Date();
        final DoxType config = doxen.get(schemaName);
        final SchemaType schema = currentSchemaMap.get(schemaName);

        final String inputJson = bson.toJson();
        validate(schema, inputJson);

        final DoxID doxId = DoxID.generate();

        bson.put("_id", new BsonString(doxId.toString()));
        bson.put("_version", new BsonInt32(1));

        final String storedJson = bson.toJson();
        final byte[] accessKey = collectionAccessControl.buildAccessKey(config.getName(), storedJson, ctx.getCallerPrincipal());

        final DoxEntity entity = new DoxEntity();
        entity.setDoxId(doxId);
        entity.setContent(bson);
        entity.setCreatedBy(ctx.getCallerPrincipal());
        entity.setCreatedOn(ts);
        entity.setLastUpdatedBy(ctx.getCallerPrincipal());
        entity.setLastUpdatedOn(ts);
        entity.setSchemaName(config.getName());
        entity.setSchemaVersion(schema.getVersion());
        entity.setAccessKey(accessKey);
        entity.setVersion(1);

        em.persist(entity);

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

    @Override
    public void delete(final String collection,
        final DoxID doxid,
        final int version) {

        final Date ts = new Date();
        final DoxType config = doxen.get(collection);
        final DoxMeta meta = readMetaAndLock(config.getName(), doxid, version);

        final DoxEntity toBeDeleted = em.find(DoxEntity.class, meta.getId());
        final DoxTombstone tombstone = toBeDeleted.buildTombstone(ctx.getCallerPrincipal(), ts);
        em.persist(tombstone);
        em.remove(toBeDeleted);

        doxSearchBean.removeFromIndex(config.getName(), doxid);
        eventHandler.onRecordDelete(config.getName(), doxid);

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

        final DoxMeta meta = em.createNamedQuery("readMetaBySchemaNameDoxID", DoxMeta.class).setParameter("doxId", doxid.toString()).setParameter("schemaName", config.getName()).getSingleResult();

        meta.getAccessKey();
        // TODO check the security.

        String contentJson;

        if (meta.getSchemaVersion() != schema.getVersion()) {
            final DoxEntity e = em.find(DoxEntity.class, meta.getId(), LockModeType.OPTIMISTIC_FORCE_INCREMENT);
            BsonDocument document = e.getContent();
            final BsonValue id = document.remove("_id");
            final BsonValue version = document.remove("_version");
            contentJson = migrator.migrate(collectionName, e.getSchemaVersion(), schema.getVersion(), document.toJson());
            document = BsonDocument.parse(contentJson);
            document.put("_id", id);
            document.put("_version", version);
            meta.setSchemaVersion(schema.getVersion());
            e.setSchemaVersion(schema.getVersion());
            contentJson = document.toJson();
            em.persist(e);
        } else {
            final DoxEntity e = em.find(DoxEntity.class, meta.getId(), LockModeType.OPTIMISTIC);
            final BsonDocument document = e.getContent();
            document.put("_id", new BsonString(e.getDoxId().toString()));
            document.put("_version", new BsonInt32(e.getVersion()));
            contentJson = document.toJson();
        }

        meta.setContentJson(contentJson);
        eventHandler.onRecordCreate(config.getName(), doxid, contentJson);
        return meta;

    }

    @Override
    public BsonArray readAll(final String collectionName) {

        final DoxType config = doxen.get(collectionName);
        if (!config.isReadAll()) {
            throw new PersistenceException("Not supported");
        }
        final SchemaType schema = currentSchemaMap.get(collectionName);

        final BsonArray all = new BsonArray();

        final List<DoxEntity> results = em.createNamedQuery("readAllBySchemaName", DoxEntity.class).setParameter("schemaName", config.getName()).getResultList();
        for (final DoxEntity result : results) {

            result.getAccessKey();
            // TODO check security
            if (result.getSchemaVersion() != schema.getVersion()) {
                migrator.migrate(config.getName(), result.getSchemaVersion(), schema.getVersion(), result.getJsonContent());
                // queue migrate later?
            } else {
                all.add(result.getContent());
            }

        }
        return all;
    }

    private DoxMeta readMetaAndLock(
        final String schemaName,
        final DoxID doxid,
        final int version) {

        try {
            return em.createNamedQuery("readForUpdateMetaBySchemaNameDoxIDVersion", DoxMeta.class).setParameter("doxId", doxid.toString()).setParameter("schemaName", schemaName).setParameter("version", version).getSingleResult();

        } catch (final NoResultException e) {
            throw new OptimisticLockException(e);
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

        final DoxMeta meta = readMetaAndLock(config.getName(), doxid, version);
        meta.incrementVersion();

        meta.getAccessKey();
        // TODO check the security.

        final String cleanJson = bson.toJson();
        final IndexView[] indexViews = indexer.buildIndexViews(config.getName(), cleanJson);

        final byte[] accessKey = collectionAccessControl.buildAccessKey(config.getName(), cleanJson, ctx.getCallerPrincipal());

        // FIXME we really shouldn't store it.
        bson.put("_id", new BsonString(doxid.toString()));
        bson.put("_version", new BsonInt32(meta.getVersion()));

        final DoxEntity e = em.find(DoxEntity.class, meta.getId());
        e.setLastUpdatedBy(ctx.getCallerPrincipal());
        e.setLastUpdatedOn(ts);
        e.setContent(bson);
        e.setAccessKey(accessKey);
        em.persist(e);
        em.flush();
        em.refresh(e);

        System.out.println("stored=" + e.getContent().toJson());

        for (final IndexView indexView : indexViews) {
            indexView.setCollection(config.getName());
            indexView.setDoxID(doxid);
        }
        doxSearchBean.addToIndex(indexViews);

        eventHandler.onRecordUpdate(config.getName(), doxid, e.getJsonContent());
        meta.setContentJson(bson.toJson());
        return meta;

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
