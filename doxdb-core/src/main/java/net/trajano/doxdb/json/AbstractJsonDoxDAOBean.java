package net.trajano.doxdb.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;

import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;

import net.trajano.doxdb.DoxConfiguration;
import net.trajano.doxdb.DoxDAO;
import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.jdbc.JdbcDoxDAO;

/**
 * JSON based Dox. This wraps the main Dox operations so that it will take in
 * JSON data. The data itself is stored in BSON to make it more efficient. This
 * will be extended by the EJBs. This does not provide extension points for the
 * operations, those operations should be done on the application specific
 * versions.
 *
 * @author Archimedes
 */
public abstract class AbstractJsonDoxDAOBean {

    private Connection connection;

    private DoxDAO dao;

    /**
     * The data source. It is required that the datasource be XA enabled so it
     * can co-exist with JPA and other operations.
     */
    @Resource(name = "doxdbDataSource", lookup = "java:comp/DefaultDataSource")
    private DataSource ds;

    protected AbstractJsonDoxDAOBean() {

    }

    /**
     * This provides an alternate constructor that will connect using a JDBC
     * connection rather than a data source for unit testing.
     *
     * @param connection
     */
    protected AbstractJsonDoxDAOBean(Connection connection) {
        this.connection = connection;
        dao = new JdbcDoxDAO(connection, buildConfiguration());
    }

    public void attach(DoxID doxId,
            String reference,
            InputStream in,
            int version,
            Principal principal) {

        dao.attach(doxId, reference, in, version, principal);
    }

    /**
     * This may be overridden by classes to support other capabilities such as
     * OOB. Otherwise it will use the "simple name" of the derived class by
     * default.
     *
     * @return
     */
    protected DoxConfiguration buildConfiguration() {

        return new DoxConfiguration(getClass().getSimpleName());
    }

    public DoxID create(JsonObject json,
            Principal principal) {

        validate(json);
        final BasicOutputBuffer basicOutputBuffer = new BasicOutputBuffer();

        new BsonDocumentCodec().encode(new BsonBinaryWriter(basicOutputBuffer), BsonDocument.parse(json.toString()), EncoderContext.builder()
                .build());
        try (final ByteArrayInputStream is = new ByteArrayInputStream(basicOutputBuffer.toByteArray())) {
            return dao.create(is, principal);
        } catch (final IOException e) {
            throw new PersistenceException(e);
        }
    }

    public void delete(DoxID id,
            int version,
            Principal principal) {

        dao.delete(id, version, principal);
    }

    public void detach(DoxID doxId,
            String reference,
            int version,
            Principal principal) {

        dao.detach(doxId, reference, version, principal);
    }

    public void exportDox(DoxID doxID,
            OutputStream os) throws IOException {

        dao.exportDox(doxID, os);

    }

    public int getVersion(DoxID id) {

        return dao.getVersion(id);
    }

    public void importDox(InputStream is) throws IOException {

        dao.importDox(is);

    }

    @PostConstruct
    public void init() {

        try {
            connection = ds.getConnection();
            dao = new JdbcDoxDAO(connection, buildConfiguration());
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public JsonObject readContent(DoxID id) {

        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            dao.readContentToStream(id, baos);
            baos.close();
            final BsonDocument decoded = new BsonDocumentCodec().decode(new BsonBinaryReader(ByteBuffer.wrap(baos.toByteArray())), DecoderContext.builder()
                    .build());
            return Json.createReader(new StringReader(decoded.toJson()))
                    .readObject();
        } catch (final IOException e) {
            throw new PersistenceException(e);
        }
    }

    public int readOobContent(DoxID doxId,
            String reference,
            ByteBuffer buffer) {

        return dao.readOobContent(doxId, reference, buffer);
    }

    public void readOobContentToStream(DoxID id,
            String reference,
            OutputStream os) throws IOException {

        dao.readOobContentToStream(id, reference, os);

    }

    /**
     * Closes the connection that was initialized.
     */
    @PreDestroy
    public void shutdown() {

        try {
            connection.close();
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public void updateContent(DoxID doxId,
            JsonObject json,
            int version,
            Principal principal) {

        final BasicOutputBuffer basicOutputBuffer = new BasicOutputBuffer();

        new BsonDocumentCodec().encode(new BsonBinaryWriter(basicOutputBuffer), BsonDocument.parse(json.toString()), EncoderContext.builder()
                .build());
        try (final ByteArrayInputStream is = new ByteArrayInputStream(basicOutputBuffer.toByteArray())) {
            dao.updateContent(doxId, is, version, principal);
        } catch (final IOException e) {
            throw new PersistenceException(e);
        }

    }

    private void validate(JsonObject json) {

        try {
            final ValidationConfiguration cfg = ValidationConfiguration.newBuilder()
                    .setDefaultVersion(SchemaVersion.DRAFTV4)
                    .freeze();
            final LoadingConfiguration loadingCfg = LoadingConfiguration.newBuilder().freeze();
            final JsonValidator validator = JsonSchemaFactory.newBuilder()
                    .setLoadingConfiguration(loadingCfg)
                    .setValidationConfiguration(cfg)
                    .freeze()
                    .getValidator();

            validator.validate(null, JsonLoader.fromString(json.toString()))
                    .isSuccess();
        } catch (final IOException | ProcessingException e) {
            throw new PersistenceException(e);
        }
    }
}
