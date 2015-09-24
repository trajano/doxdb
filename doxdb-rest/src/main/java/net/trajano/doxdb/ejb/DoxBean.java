package net.trajano.doxdb.ejb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.validation.ValidationException;

import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;

import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;

import net.trajano.doxdb.Dox;
import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxMeta;
import net.trajano.doxdb.DoxTombstone;
import net.trajano.doxdb.IndexView;
import net.trajano.doxdb.SearchResult;
import net.trajano.doxdb.ext.CollectionAccessControl;
import net.trajano.doxdb.ext.ConfigurationProvider;
import net.trajano.doxdb.ext.EventHandler;
import net.trajano.doxdb.ext.Indexer;
import net.trajano.doxdb.ext.Migrator;
import net.trajano.doxdb.schema.DoxPersistence;
import net.trajano.doxdb.schema.DoxType;
import net.trajano.doxdb.schema.ReadAllType;
import net.trajano.doxdb.schema.SchemaType;

/**
 * Implements the DoxDB persistence operations. An EJB is used to take advantage
 * of transaction management that is provided by EJBs.
 *
 * @author Archimedes Trajano
 */
@Stateless
@Dependent
@LocalBean
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

    private DoxSearch doxSearchBean;

    private EntityManager em;

    private EventHandler eventHandler;

    private Indexer indexer;

    private Migrator migrator;

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
    public SearchResult advancedSearch(final String index,
        final String schemaName,
        final JsonObject query) {

        return doxSearchBean.advancedSearch(index, schemaName, query);
    }

    @Override
    public DoxMeta create(final String schemaName,
        final BsonDocument bson) {

        final Date ts = new Date();
        final DoxType config = configurationProvider.getDox(schemaName);
        final SchemaType schema = configurationProvider.getCollectionSchema(schemaName);

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
        entity.setCollectionName(config.getName());
        entity.setCollectionSchemaVersion(schema.getVersion());
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
        final DoxType config = configurationProvider.getDox(collectionName);
        final DoxMeta meta = readMetaAndLock(config.getName(), doxid, version);

        meta.getAccessKey();
        // TODO check the security.

        final Dox toBeDeleted = em.find(Dox.class, meta.getId());
        if (toBeDeleted == null) {
            return false;
        }
        final BsonDocument contentBson = toBeDeleted.getContent();
        final DoxTombstone tombstone = toBeDeleted.buildTombstone(ctx.getCallerPrincipal(), ts);
        em.persist(tombstone);
        em.remove(toBeDeleted);

        final SchemaType schema = configurationProvider.getCollectionSchema(collectionName);

        String contentJson = contentBson.toJson();
        if (meta.getCollectionSchemaVersion() != schema.getVersion()) {
            contentJson = migrator.migrate(collectionName, meta.getCollectionSchemaVersion(), schema.getVersion(), contentJson);
        }

        doxSearchBean.removeFromIndex(config.getName(), doxid);
        eventHandler.onRecordDelete(config.getName(), doxid, contentJson);
        return true;

    }

    @Override
    public DoxPersistence getConfiguration() {

        return configurationProvider.getPersistenceConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getSchema(final String path) {

        return Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/schema/" + path);
    }

    @Override
    public void noop() {

    }

    @Override
    public DoxMeta read(final String collectionName,
        final DoxID doxid) {

        final DoxType config = configurationProvider.getDox(collectionName);
        final SchemaType schema = configurationProvider.getCollectionSchema(collectionName);

        final DoxMeta meta = em.createNamedQuery(Dox.READ_META_BY_SCHEMA_NAME_DOX_ID, DoxMeta.class).setParameter("doxId", doxid.toString()).setParameter("collectionName", config.getName()).getSingleResult();

        meta.getAccessKey();
        // TODO check the security.

        String contentJson;

        if (meta.getCollectionSchemaVersion() != schema.getVersion()) {
            final Dox e = em.find(Dox.class, meta.getId(), LockModeType.OPTIMISTIC_FORCE_INCREMENT);
            contentJson = migrator.migrate(collectionName, e.getCollectionSchemaVersion(), schema.getVersion(), e.getJsonContent());
            final BsonDocument document = BsonDocument.parse(contentJson);
            meta.setCollectionSchemaVersion(schema.getVersion());
            e.setCollectionSchemaVersion(schema.getVersion());
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
    public String readAll(final String collectionName) {

        final DoxType config = configurationProvider.getDox(collectionName);
        if (config.getReadAll() == ReadAllType.FILE) {
            try {
                return readAllToFile(config.getName());
            } catch (final IOException e) {
                throw new PersistenceException(e);
            }
        } else if (config.getReadAll() == ReadAllType.MEMORY) {
            return readAllToString(config.getName());
        } else {
            throw new PersistenceException("Not supported");
        }

    }

    private String readAllToFile(final String schemaName) throws IOException {

        final SchemaType schema = configurationProvider.getCollectionSchema(schemaName);

        final File f = File.createTempFile("doxdb", schemaName);

        try (final Writer os = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(f)), "UTF-8")) {
            os.write('[');

            final List<Dox> results = em.createNamedQuery(Dox.READ_ALL_BY_SCHEMA_NAME, Dox.class).setParameter("schemaName", schemaName).getResultList();
            final Iterator<Dox> i = results.iterator();
            while (i.hasNext()) {

                final Dox result = i.next();
                final boolean last = !i.hasNext();
                result.getAccessKey();
                // TODO check security
                if (result.getCollectionSchemaVersion() != schema.getVersion()) {
                    migrator.migrate(schemaName, result.getCollectionSchemaVersion(), schema.getVersion(), result.getJsonContent());
                    // queue migrate later?
                } else {
                    os.write(addMeta(result.getContent(), result.getDoxId(), result.getVersion()).toJson());
                    if (!last) {
                        os.write(',');
                    }
                }

            }
            os.write(']');
        }
        return f.getCanonicalPath();

    }

    private String readAllToString(final String schemaName) {

        final SchemaType schema = configurationProvider.getCollectionSchema(schemaName);

        final StringBuilder b = new StringBuilder("[");

        final List<Dox> results = em.createNamedQuery(Dox.READ_ALL_BY_SCHEMA_NAME, Dox.class).setParameter("schemaName", schemaName).getResultList();
        for (final Dox result : results) {

            result.getAccessKey();
            // TODO check security
            if (result.getCollectionSchemaVersion() != schema.getVersion()) {
                migrator.migrate(schemaName, result.getCollectionSchemaVersion(), schema.getVersion(), result.getJsonContent());
                // queue migrate later?
            } else {
                b.append(addMeta(result.getContent(), result.getDoxId(), result.getVersion()).toJson());
                b.append(',');
            }

        }
        if (b.length() > 1) {
            b.replace(b.length() - 1, b.length(), "]");
        } else {
            b.append(']');
        }
        return b.toString();

    }

    private DoxMeta readMetaAndLock(
        final String schemaName,
        final DoxID doxid,
        final int version) {

        try {
            return em.createNamedQuery(Dox.READ_FOR_UPDATE_META_BY_SCHEMA_NAME_DOX_ID_VERSION, DoxMeta.class).setParameter("doxId", doxid.toString()).setParameter("schemaName", schemaName).setParameter("version", version).getSingleResult();

        } catch (final NoResultException e) {
            throw new OptimisticLockException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Asynchronous
    public void reindex() {

        doxSearchBean.reset();
        // TODO this will do everything in one transaction which can kill the database.  What could be done is the
        // reindexing can be done in chunks and let an MDB do the process
        for (final DoxType config : configurationProvider.getPersistenceConfig().getDox()) {

            final List<IndexView> indexViews = new LinkedList<>();
            for (final Dox e : em.createNamedQuery(Dox.READ_ALL_BY_SCHEMA_NAME, Dox.class).setParameter("schemaName", config.getName()).getResultList()) {

                // TODO later
                //                rs.updateBytes(3, collectionAccessControl.buildAccessKey(config.getName(), json, new DoxPrincipal(rs.getString(4))));
                final IndexView[] indexViewBuilt = indexer.buildIndexViews(config.getName(), e.getJsonContent());
                for (final IndexView indexView : indexViewBuilt) {
                    indexView.setCollection(e.getCollectionName());
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

    @Override
    public SearchResult searchWithSchemaName(final String index,
        final String schemaName,
        final String queryString,
        final int limit,
        final Integer fromDoc) {

        return doxSearchBean.searchWithSchemaName(index, schemaName, queryString, limit, fromDoc);
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
        final DoxType config = configurationProvider.getDox(collectionName);
        final SchemaType schema = configurationProvider.getCollectionSchema(collectionName);

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

    /**
     * Performs JSON validation using a schema
     *
     * @param schema
     *            schema
     * @param json
     *            json to validate.
     */
    private void validate(final SchemaType schema,
        final String json) {

        try {

            final JsonSchema jsonSchema = configurationProvider.getContentSchema(schema.getLocation());

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
