package net.trajano.doxdb.sample.test;

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

        JsonSchema schema = JsonSchemaFactory.newBuilder()
                .setValidationConfiguration(cfg)
                .freeze()
                .getJsonSchema(JsonLoader.fromResource("/schema/horse.json"));

        ProcessingReport validate = schema.validate(JsonLoader.fromString("{\"name\":\"archie\"}"));
        Assert.assertTrue(validate.toString(), validate.isSuccess());

    }
}
