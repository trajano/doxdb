package net.trajano.doxdb.json;

import java.io.IOException;
import java.security.Principal;

import javax.annotation.PostConstruct;
import javax.json.JsonObject;
import javax.persistence.PersistenceException;

import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import net.trajano.doxdb.DoxID;

public abstract class AbstractValidatingJsonDoxDAOBean extends AbstractJsonDoxDAOBean {

    private JsonSchema schema;

    @Override
    public DoxID create(JsonObject json,
            Principal principal) {

        validate(json);
        return super.create(json, principal);
    }

    /**
     * Extended by implementers to specify the schema resource used for
     * validation.
     *
     * @return list of resources.
     */
    protected abstract String getSchemaResource();

    @Override
    @PostConstruct
    public void init() {

        super.init();
        try {
            final ValidationConfiguration cfg = ValidationConfiguration.newBuilder()
                    .setDefaultVersion(SchemaVersion.DRAFTV4)
                    .freeze();

            schema = JsonSchemaFactory.newBuilder()
                    .setValidationConfiguration(cfg)
                    .freeze()
                    .getJsonSchema(JsonLoader.fromResource(getSchemaResource()));
        } catch (ProcessingException | IOException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void updateContent(DoxID doxId,
            JsonObject json,
            int version,
            Principal principal) {

        validate(json);
        super.updateContent(doxId, json, version, principal);

    }

    private void validate(JsonObject json) {

        try {
            final ProcessingReport validate = schema.validate(JsonLoader.fromString(json.toString()));
            if (!validate.isSuccess()) {
                throw new PersistenceException(validate.toString());
            }
        } catch (ProcessingException | IOException e) {
            throw new PersistenceException(e);
        }
    }

}
