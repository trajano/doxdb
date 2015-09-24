package net.trajano.doxdb.sample.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;

import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

public class JsonValidationTest {

    @Test
    public void testJson() throws Exception {

        final ValidationConfiguration cfg = ValidationConfiguration.newBuilder()
            .setDefaultVersion(SchemaVersion.DRAFTV4)
            .freeze();

        final JsonSchema schema = JsonSchemaFactory.newBuilder()
            .setValidationConfiguration(cfg)
            .freeze()
            .getJsonSchema(JsonLoader.fromResource("/META-INF/schema/horse.json"));

        final ProcessingReport validate = schema.validate(JsonLoader.fromString("{\"name\":\"archie\"}"));
        Assert.assertTrue(validate.toString(), validate.isSuccess());

    }

    @Test
    public void testJsonWithBadExtra() throws Exception {

        final ValidationConfiguration cfg = ValidationConfiguration.newBuilder()
            .setDefaultVersion(SchemaVersion.DRAFTV4)
            .freeze();

        final JsonSchema schema = JsonSchemaFactory.newBuilder()
            .setValidationConfiguration(cfg)
            .freeze()
            .getJsonSchema(JsonLoader.fromResource("/META-INF/schema/horse.json"));

        final ProcessingReport validate = schema.validate(JsonLoader.fromString("{\"name\":\"archie\", \"_id\":\"mazui\" }"));
        assertTrue(validate.toString(), validate.isSuccess());

    }

    @Test
    public void testJsonWithExtra() throws Exception {

        final ValidationConfiguration cfg = ValidationConfiguration.newBuilder()
            .setDefaultVersion(SchemaVersion.DRAFTV4)
            .freeze();

        final JsonSchema schema = JsonSchemaFactory.newBuilder()
            .setValidationConfiguration(cfg)
            .freeze()
            .getJsonSchema(JsonLoader.fromResource("/META-INF/schema/horse.json"));

        final ProcessingReport validate = schema.validate(JsonLoader.fromString("{\"name\":\"archie\", \"_XX\":\"mazui\" }"));
        Assert.assertFalse(validate.toString(), validate.isSuccess());

    }

    @Test
    public void testMediaType() {

        assertEquals("application/json;charset=utf-8", MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8").toString());
    }
}
