package net.trajano.doxdb.ejb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Principal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.context.Dependent;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.validation.ValidationException;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.DatatypeConverter;

import org.bson.BsonBinaryReader;
import org.bson.BsonDocument;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.json.JsonWriterSettings;

import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;

import net.trajano.doxdb.Dox;
import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxLookup;
import net.trajano.doxdb.DoxUnique;
import net.trajano.doxdb.IndexView;
import net.trajano.doxdb.ext.CollectionAccessControl;
import net.trajano.doxdb.ext.ConfigurationProvider;
import net.trajano.doxdb.ext.EventHandler;
import net.trajano.doxdb.ext.Indexer;
import net.trajano.doxdb.ext.Migrator;
import net.trajano.doxdb.internal.DoxPrincipal;
import net.trajano.doxdb.jsonpath.JsonPath;
import net.trajano.doxdb.schema.CollectionType;
import net.trajano.doxdb.schema.LookupType;
import net.trajano.doxdb.schema.SchemaType;

@TransactionManagement(TransactionManagementType.BEAN)
@Stateless
@Dependent
@LocalBean
@Local(DoxImport.class)
public class DoxImportBean {

    private static Path buildFromCollectionAndDoxID(final String collectionName,
        final String doxIdString) {

        return Paths.get(collectionName, doxIdString.substring(0, 2), doxIdString.substring(2, 4), doxIdString.substring(4, 6));
    }

    private CollectionAccessControl collectionAccessControl;

    private ConfigurationProvider configurationProvider;

    @Resource(name = "doxDataSource",
        lookup = "java:comp/DefaultDataSource")
    private DataSource doxDataSource;

    private DoxSearch doxSearchBean;

    private EntityManager em;

    private EventHandler eventHandler;

    private Indexer indexer;

    private Migrator migrator;

    @Resource
    private UserTransaction txn;

    public JsonObject exportDox(final String exportPath,
        final String schema,
        final Date fromLastUpdatedOn) {

        final Path basePath = Paths.get(exportPath);
        final JsonObjectBuilder stats = Json.createObjectBuilder();
        final long start = System.currentTimeMillis();
        try {
            Files.createDirectories(basePath);

            if (!Files.isDirectory(basePath) ||
                !Files.isExecutable(basePath) ||
                !Files.isWritable(basePath)) {
                throw new PersistenceException("Unable to access export path");
            }

            if (fromLastUpdatedOn == null && Files.newDirectoryStream(basePath).iterator().hasNext()) {
                throw new PersistenceException("Export path must be empty when starting date is not specified");
            }
            txn.begin();

            final String dbSchema;
            if (schema == null) {
                dbSchema = "";
            } else {
                dbSchema = schema + ".";
            }
            try (final Connection connection = doxDataSource.getConnection()) {

                int c = 0;
                Timestamp mostRecentUpdateOn = null;
                try (final PreparedStatement stmt = connection.prepareStatement("select collectionName, collectionSchemaVersion, doxid, content, createdOn, createdBy, lastupdatedOn, lastUpdatedBy, id from " + dbSchema + "dox where ? or lastUpdatedOn >= ?")) {
                    if (fromLastUpdatedOn == null) {
                        stmt.setBoolean(1, true);
                        stmt.setTimestamp(2, new Timestamp(0));
                    } else {
                        stmt.setBoolean(1, false);
                        stmt.setTimestamp(2, new Timestamp(fromLastUpdatedOn.getTime()));
                    }

                    try (final ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            final String collectionName = rs.getString("COLLECTIONNAME");
                            final String doxIdString = rs.getString("DOXID");
                            final Timestamp lastUpdatedOn = rs.getTimestamp("LASTUPDATEDON");
                            if (mostRecentUpdateOn == null || lastUpdatedOn.after(mostRecentUpdateOn)) {
                                mostRecentUpdateOn = lastUpdatedOn;
                            }

                            final MimeMultipart mimeMultipart = new MimeMultipart();
                            mimeMultipart.setSubType("mixed");

                            exportMainContentToMultipart(mimeMultipart, rs);
                            try (final PreparedStatement oobStmt = connection.prepareStatement("select oobName, content, createdOn, createdBy, lastupdatedOn, lastUpdatedBy from " + dbSchema + "doxoob where parentid = ?")) {
                                oobStmt.setLong(1, rs.getLong("ID"));
                                try (final ResultSet oobRs = oobStmt.executeQuery()) {
                                    while (oobRs.next()) {
                                        exportOobContentToMultipart(mimeMultipart, oobRs);
                                    }
                                }
                            }

                            final Path outputPath = basePath.resolve(buildFromCollectionAndDoxID(collectionName, doxIdString));
                            Files.createDirectories(outputPath);
                            try (final OutputStream os = Files.newOutputStream(outputPath.resolve(doxIdString + ".dox"))) {
                                mimeMultipart.writeTo(os);
                            }
                            ++c;
                        }
                    }

                }
                if (mostRecentUpdateOn != null) {
                    final Calendar mostRecentUpdateOnCal = Calendar.getInstance();
                    mostRecentUpdateOnCal.setTimeInMillis(mostRecentUpdateOn.getTime());
                    stats.add("most_recent_update_on", DatatypeConverter.printDateTime(mostRecentUpdateOnCal));
                }
                stats.add("number_of_exported_documents", c);
                txn.commit();
            } catch (SecurityException
                | IllegalStateException
                | RollbackException
                | IOException
                | SQLException
                | MessagingException
                | HeuristicMixedException
                | HeuristicRollbackException e) {
                txn.rollback();
                throw new PersistenceException(e);
            }
        } catch (final SystemException
            | IOException
            | NotSupportedException e) {
            throw new PersistenceException(e);
        }
        stats.add("elapsed_time_millis", System.currentTimeMillis() - start);
        return stats.build();

    }

    /**
     * @param mimeMultipart
     * @param rs
     * @throws SQLException
     * @throws MessagingException
     */
    private void exportMainContentToMultipart(final MimeMultipart mimeMultipart,
        final ResultSet rs) throws SQLException,
            MessagingException {

        {
            final String collectionName = rs.getString("COLLECTIONNAME");
            final String collectionSchemaVersion = String.valueOf(rs.getInt("COLLECTIONSCHEMAVERSION"));
            final String doxIdString = rs.getString("DOXID");

            final BsonDocument decoded = new BsonDocumentCodec().decode(new BsonBinaryReader(ByteBuffer.wrap(rs.getBytes("CONTENT"))), DecoderContext.builder()
                .build());
            final MimeBodyPart mimeBodyPart = new MimeBodyPart();
            final String json = decoded.toJson(new JsonWriterSettings(true));
            mimeBodyPart.setText(json, "UTF-8", "json");

            mimeBodyPart.setHeader("Collection-Name", collectionName);
            mimeBodyPart.setHeader("Collection-Schema-Version", collectionSchemaVersion);

            mimeBodyPart.setHeader("Created-By", rs.getString("CREATEDBY"));
            final Calendar createdOn = Calendar.getInstance();
            createdOn.setTimeInMillis(rs.getTimestamp("CREATEDON").getTime());
            mimeBodyPart.setHeader("Created-On", DatatypeConverter.printDateTime(createdOn));

            final Calendar lastUpdatedOn = Calendar.getInstance();
            lastUpdatedOn.setTimeInMillis(rs.getTimestamp("LASTUPDATEDON").getTime());
            mimeBodyPart.setHeader("Last-Updated-On", DatatypeConverter.printDateTime(lastUpdatedOn));
            mimeBodyPart.setHeader("Last-Updated-By", rs.getString("LASTUPDATEDBY"));

            mimeBodyPart.setHeader("Content-Length",
                String.valueOf(json.length()));
            mimeBodyPart.setFileName(doxIdString);
            mimeMultipart.addBodyPart(mimeBodyPart);
        }
    }

    private void exportOobContentToMultipart(final MimeMultipart mimeMultipart,
        final ResultSet rs) throws MessagingException,
            SQLException {

        final Blob contentBlob = rs.getBlob("CONTENT");
        final MimeBodyPart mimeBodyPart = new MimeBodyPart(contentBlob.getBinaryStream());
        mimeBodyPart.setFileName(rs.getString("OOBNAME"));

        mimeBodyPart.setHeader("Created-By", rs.getString("CREATEDBY"));
        final Calendar createdOn = Calendar.getInstance();
        createdOn.setTimeInMillis(rs.getTimestamp("CREATEDON").getTime());
        mimeBodyPart.setHeader("Created-On", DatatypeConverter.printDateTime(createdOn));

        final Calendar lastUpdatedOn = Calendar.getInstance();
        lastUpdatedOn.setTimeInMillis(rs.getTimestamp("LASTUPDATEDON").getTime());
        mimeBodyPart.setHeader("Last-Updated-On", DatatypeConverter.printDateTime(lastUpdatedOn));
        mimeBodyPart.setHeader("Last-Updated-By", rs.getString("LASTUPDATEDBY"));
        mimeMultipart.addBodyPart(mimeBodyPart);

        contentBlob.free();

    }

    public JsonObject importDox(final String importPath) {

        final JsonObjectBuilder stats = Json.createObjectBuilder();
        final long start = System.currentTimeMillis();
        try {
            txn.begin();

            final long numberOfRecords = em.createNamedQuery(Dox.COUNT, Long.class).getSingleResult();
            if (numberOfRecords > 0) {
                txn.rollback();
                throw new PersistenceException("cannot import when Dox is not empty");
            }

            final Path basePath = Paths.get(importPath);
            if (!Files.isDirectory(basePath) ||
                !Files.isExecutable(basePath) ||
                !Files.isReadable(basePath)) {
                txn.rollback();
                throw new PersistenceException("Unable to access import path");
            }

            // collect the list of files first because Files.walk is not guaranteed to return in order.
            final SortedSet<Path> files = new TreeSet<>();

            Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(final Path file,
                    final BasicFileAttributes attrs) throws IOException {

                    files.add(file);
                    return FileVisitResult.CONTINUE;
                }

            });

            final JsonArrayBuilder failures = Json.createArrayBuilder();
            int c = 0;
            for (final Path file : files) {
                try {
                    processFile(file);
                } catch (IOException
                    | MessagingException
                    | NullPointerException
                    | PersistenceException e) {
                    txn.rollback();
                    final JsonObjectBuilder failure = Json.createObjectBuilder();
                    failure.add("file", file.toString());
                    failure.add("reason", e.getMessage());
                    final StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    failure.add("stacktrace", sw.toString());
                    failures.add(failure);
                    txn.begin();
                }
                ++c;
                if (c != 0 && c % 10 == 0) {
                    txn.commit();
                    txn.begin();
                }
            }
            stats.add("number_of_imported_documents", c);
            stats.add("failures", failures);
            txn.commit();
        } catch (final IOException
            | SystemException
            | NotSupportedException
            | SecurityException
            | IllegalStateException
            | RollbackException
            | HeuristicMixedException
            | HeuristicRollbackException e) {
            throw new PersistenceException(e);
        }
        stats.add("elapsed_time_millis", System.currentTimeMillis() - start);
        return stats.build();
    }

    private void processFile(final Path file) throws IOException,
        MessagingException {

        try (InputStream is = Files.newInputStream(file)) {

            final MimeMultipart mmp = new MimeMultipart(new ByteArrayDataSource(is, MediaType.MULTIPART_FORM_DATA));
            if (mmp.getCount() == 0) {
                throw new PersistenceException("No data was found for import");
            }
            final BodyPart mainBody = mmp.getBodyPart(0);

            final DoxID doxId = new DoxID(mainBody.getFileName());

            final String collectionName = mainBody.getHeader("Collection-Name")[0];
            final int collectionSchemaVersion = Integer.valueOf(mainBody.getHeader("Collection-Schema-Version")[0]);

            final Principal createdBy = new DoxPrincipal(mainBody.getHeader("Created-By")[0]);
            final Principal lastUpdatedBy = new DoxPrincipal(mainBody.getHeader("Last-Updated-By")[0]);

            final Date createdOn = new Date(DatatypeConverter.parseDateTime(mainBody.getHeader("Created-On")[0])
                .getTimeInMillis());
            final Date lastUpdatedOn = new Date(DatatypeConverter.parseDateTime(mainBody.getHeader("Last-Updated-On")[0])
                .getTimeInMillis());

            final CollectionType config = configurationProvider.getDox(collectionName);
            final SchemaType schema = configurationProvider.getCollectionSchema(collectionName);

            final JsonObject content = Json.createReader(mainBody.getInputStream()).readObject();
            validate(schema, content);

            final String inputJson = content.toString();
            final byte[] accessKey = collectionAccessControl.buildAccessKey(collectionName, inputJson, lastUpdatedBy.getName());

            final Dox entity = new Dox();
            entity.setDoxId(doxId);
            entity.setContent(content);
            entity.setCreatedBy(createdBy);
            entity.setCreatedOn(createdOn);
            entity.setLastUpdatedBy(lastUpdatedBy);
            entity.setLastUpdatedOn(lastUpdatedOn);
            entity.setCollectionName(collectionName);
            entity.setCollectionSchemaVersion(collectionSchemaVersion);
            entity.setAccessKey(accessKey);
            entity.setVersion(1);
            System.out.println(entity);

            em.persist(entity);

            for (final LookupType unique : schema.getUnique()) {
                final String lookupKey = JsonPath.compile(unique.getPath()).read(inputJson);
                final DoxUnique doxUnique = new DoxUnique();
                doxUnique.setCollectionName(config.getName());
                doxUnique.setDox(entity);
                doxUnique.setLookupName(unique.getName());
                doxUnique.setLookupKey(lookupKey);
                em.persist(doxUnique);
            }
            for (final LookupType unique : schema.getLookup()) {
                final String lookupKey = JsonPath.compile(unique.getPath()).read(inputJson);
                final DoxLookup doxLookup = new DoxLookup();
                doxLookup.setCollectionName(config.getName());
                doxLookup.setDox(entity);
                doxLookup.setLookupName(unique.getName());
                doxLookup.setLookupKey(lookupKey);
                em.persist(doxLookup);
            }

            eventHandler.onRecordCreate(config.getName(), doxId, inputJson);

            final IndexView[] indexViews = indexer.buildIndexViews(config.getName(), inputJson);
            for (final IndexView indexView : indexViews) {
                indexView.setCollection(config.getName());
                indexView.setDoxID(doxId);
            }
            if (indexViews.length > 0) {
                doxSearchBean.addToIndex(indexViews);
            }
        }

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

    private void validate(final SchemaType schema,
        final JsonObject content) {

        validate(schema, content.toString());

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
