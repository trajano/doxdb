package net.trajano.doxdb.ejb;

import java.io.IOException;
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
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.validation.ValidationException;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;

import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxMeta;
import net.trajano.doxdb.IndexView;
import net.trajano.doxdb.SearchResult;
import net.trajano.doxdb.ext.CollectionAccessControl;
import net.trajano.doxdb.ext.ConfigurationProvider;
import net.trajano.doxdb.ext.DoxSearch;
import net.trajano.doxdb.ext.EventHandler;
import net.trajano.doxdb.ext.Indexer;
import net.trajano.doxdb.ext.Migrator;
import net.trajano.doxdb.schema.DoxPersistence;
import net.trajano.doxdb.schema.DoxType;
import net.trajano.doxdb.schema.SchemaType;

@Stateless
@Dependent
public class DoxBean implements
    DoxLocal {

    private CollectionAccessControl collectionAccessControl;

    private ConfigurationProvider configurationProvider;

    /**
     * Session context. It is injected here rather than
     * {@link #setSessionContext(SessionContext)} as the WebSphere tools flag
     * that using the setter is not valid incorrectly, but will still work in
     * this fasion as well.
     */
    @Resource
    private SessionContext ctx;

    private transient Map<String, SchemaType> currentSchemaMap = new HashMap<>();

    private transient Map<String, DoxType> doxen = new HashMap<>();

    private DoxSearch doxSearchBean;

    private EntityManager em;

    private EventHandler eventHandler;

    private Indexer indexer;

    private transient ConcurrentMap<String, JsonSchema> jsonSchemaMap = new ConcurrentHashMap<>();

    private Migrator migrator;

    private transient DoxPersistence persistenceConfig;

    /**
     * Adds the meta fields to the BSON document that is being returned.
     *
     * @param document
     *            document to update (will be modified).
     * @param doxId
     * @param version
     * @return updated document
     */
    private BsonDocument addMeta(final BsonDocument document,
        final DoxID doxId,
        final int version) {

        document.put("_id", new BsonString(doxId.toString()));
        document.put("_version", new BsonInt32(version));
        return document;
    }

    @Override
    public DoxMeta create(final String schemaName,
        final BsonDocument bson) {

        final Date ts = new Date();
        final DoxType config = doxen.get(schemaName);
        final SchemaType schema = currentSchemaMap.get(schemaName);

        final String inputJson = bson.toJson();
        validate(schema, inputJson);

        final DoxID doxId = DoxID.generate();

        final byte[] accessKey = collectionAccessControl.buildAccessKey(config.getName(), inputJson, ctx.getCallerPrincipal().getName());

        final Dox entity = new Dox();
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

        addMeta(bson, doxId, 1);
        meta.setContentJson(bson.toJson());

        eventHandler.onRecordCreate(config.getName(), doxId, inputJson);
        return meta;
    }

    @Override
    public boolean delete(final String collectionName,
        final DoxID doxid,
        final int version) {

        final Date ts = new Date();
        final DoxType config = doxen.get(collectionName);
        final DoxMeta meta = readMetaAndLock(config.getName(), doxid, version);

        meta.getAccessKey();
        // TODO check the security.

        final Dox toBeDeleted = em.find(Dox.class, meta.getId());
        if (toBeDeleted == null) {
            return false;
        }
        final DoxTombstone tombstone = toBeDeleted.buildTombstone(ctx.getCallerPrincipal(), ts);
        em.persist(tombstone);
        em.remove(toBeDeleted);

        final SchemaType schema = currentSchemaMap.get(collectionName);

        String contentJson;

        if (meta.getSchemaVersion() != schema.getVersion()) {
            final Dox e = em.find(Dox.class, meta.getId(), LockModeType.OPTIMISTIC_FORCE_INCREMENT);
            contentJson = migrator.migrate(collectionName, e.getSchemaVersion(), schema.getVersion(), e.getJsonContent());
            final BsonDocument document = BsonDocument.parse(contentJson);
            meta.setSchemaVersion(schema.getVersion());
            e.setSchemaVersion(schema.getVersion());
            contentJson = document.toJson();
            em.persist(e);
        } else {
            final Dox e = em.find(Dox.class, meta.getId(), LockModeType.OPTIMISTIC);
            final BsonDocument document = e.getContent();
            addMeta(document, e.getDoxId(), e.getVersion());
            contentJson = document.toJson();
        }

        doxSearchBean.removeFromIndex(config.getName(), doxid);
        eventHandler.onRecordDelete(config.getName(), doxid, contentJson);
        return true;

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
            final Dox e = em.find(Dox.class, meta.getId(), LockModeType.OPTIMISTIC_FORCE_INCREMENT);
            contentJson = migrator.migrate(collectionName, e.getSchemaVersion(), schema.getVersion(), e.getJsonContent());
            final BsonDocument document = BsonDocument.parse(contentJson);
            meta.setSchemaVersion(schema.getVersion());
            e.setSchemaVersion(schema.getVersion());
            contentJson = document.toJson();
            em.persist(e);
        } else {
            final Dox e = em.find(Dox.class, meta.getId(), LockModeType.OPTIMISTIC);
            final BsonDocument document = e.getContent();
            addMeta(document, e.getDoxId(), e.getVersion());
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

        final List<Dox> results = em.createNamedQuery("readAllBySchemaName", Dox.class).setParameter("schemaName", config.getName()).getResultList();
        for (final Dox result : results) {

            result.getAccessKey();
            // TODO check security
            if (result.getSchemaVersion() != schema.getVersion()) {
                migrator.migrate(config.getName(), result.getSchemaVersion(), schema.getVersion(), result.getJsonContent());
                // queue migrate later?
            } else {
                all.add(addMeta(result.getContent(), result.getDoxId(), result.getVersion()));
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

        // TODO this will do everything in one transaction which can kill the database.  What could be done is the
        // reindexing can be done in chunks and let an MDB do the process
        for (final DoxType config : doxen.values()) {

            final List<IndexView> indexViews = new LinkedList<>();
            for (final Dox e : em.createNamedQuery("readAllBySchemaName", Dox.class).setParameter("schemaName", config.getName()).getResultList()) {

                // TODO later
                //                rs.updateBytes(3, collectionAccessControl.buildAccessKey(config.getName(), json, new DoxPrincipal(rs.getString(4))));
                final IndexView[] indexViewBuilt = indexer.buildIndexViews(config.getName(), e.getJsonContent());
                for (final IndexView indexView : indexViewBuilt) {
                    indexView.setCollection(e.getSchemaName());
                    indexView.setDoxID(e.getDoxId());
                    indexViews.add(indexView);
                }

            }
            doxSearchBean.addToIndex(indexViews.toArray(new IndexView[0]));

        }

    }

    @Override
    public SearchResult search(final String index,
        final String queryString,
        final int limit) {

        return doxSearchBean.search(index, queryString, limit, null);
    }

    @Override
    public SearchResult search(final String index,
        final String queryString,
        final int limit,
        final Integer fromDoc) {

        return doxSearchBean.search(index, queryString, limit, fromDoc);
    }

    @EJB
    public void setCollectionAccessControl(final CollectionAccessControl collectionAccessControl) {

        this.collectionAccessControl = collectionAccessControl;
    }

    @EJB
    public void setConfigurationProvider(final ConfigurationProvider configurationProvider) {

        this.configurationProvider = configurationProvider;
    }

    @EJB
    public void setDoxSearchBean(final DoxSearch doxSearchBean) {

        this.doxSearchBean = doxSearchBean;
    }

    /**
     * Injects the {@link EntityManager}.
     *
     * @param em
     *            entity manager
     */
    @PersistenceContext
    public void setEntityManager(final EntityManager em) {

        this.em = em;
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

    public void setSessionContext(final SessionContext ctx) {

        this.ctx = ctx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoxMeta update(final String collectionName,
        final DoxID doxid,
        final BsonDocument bson,
        final int version) {

        final Timestamp ts = new Timestamp(System.currentTimeMillis());
        final DoxType config = doxen.get(collectionName);
        final SchemaType schema = currentSchemaMap.get(collectionName);

        final String inputJson = bson.toJson();
        validate(schema, inputJson);

        final DoxMeta meta = readMetaAndLock(config.getName(), doxid, version);
        meta.incrementVersion();

        meta.getAccessKey();
        // TODO check the security.

        final IndexView[] indexViews = indexer.buildIndexViews(config.getName(), inputJson);

        final byte[] accessKey = collectionAccessControl.buildAccessKey(config.getName(), inputJson, ctx.getCallerPrincipal().getName());

        final Dox e = em.find(Dox.class, meta.getId());
        e.setLastUpdatedBy(ctx.getCallerPrincipal());
        e.setLastUpdatedOn(ts);
        e.setContent(bson);
        e.setAccessKey(accessKey);
        em.persist(e);
        em.flush();
        em.refresh(e);

        for (final IndexView indexView : indexViews) {
            indexView.setCollection(config.getName());
            indexView.setDoxID(doxid);
        }
        doxSearchBean.addToIndex(indexViews);

        eventHandler.onRecordUpdate(config.getName(), doxid, e.getJsonContent());
        addMeta(bson, doxid, e.getVersion());
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
                throw new ValidationException(validate.toString());
            }
        } catch (ProcessingException
            | IOException e) {
            throw new PersistenceException(e);
        }
    }

}
