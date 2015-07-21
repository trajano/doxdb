package net.trajano.doxdb.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.security.Principal;

import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.PersistenceException;

import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;

import net.trajano.doxdb.AbstractDoxDAOBean;
import net.trajano.doxdb.DoxID;

/**
 * JSON based Dox. This wraps the main Dox operations so that it will take in
 * JSON data. The data itself is stored in BSON to make it more efficient. This
 * will be extended by the EJBs. This does not provide extension points for the
 * operations, those operations should be done on the application specific
 * versions.
 *
 * @author Archimedes
 */
public abstract class AbstractJsonDoxDAOBean extends AbstractDoxDAOBean {

    public DoxID create(JsonObject json,
            Principal principal) {

        final BasicOutputBuffer basicOutputBuffer = new BasicOutputBuffer();

        new BsonDocumentCodec().encode(new BsonBinaryWriter(basicOutputBuffer), BsonDocument.parse(json.toString()), EncoderContext.builder()
                .build());
        try (final ByteArrayInputStream is = new ByteArrayInputStream(basicOutputBuffer.toByteArray())) {
            return getDao().create(is,1,principal);
        } catch (final IOException e) {
            throw new PersistenceException(e);
        }
    }

    public JsonObject readContent(DoxID id) {

        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            getDao().readContentToStream(id, baos);
            baos.close();
            final BsonDocument decoded = new BsonDocumentCodec().decode(new BsonBinaryReader(ByteBuffer.wrap(baos.toByteArray())), DecoderContext.builder()
                    .build());
            return Json.createReader(new StringReader(decoded.toJson()))
                    .readObject();
        } catch (final IOException e) {
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
            getDao().updateContent(doxId, is,1, version, principal);
        } catch (final IOException e) {
            throw new PersistenceException(e);
        }

    }

}
