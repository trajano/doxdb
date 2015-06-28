package net.trajano.doxdb.json;

import java.security.Principal;

import javax.json.JsonObject;

import net.trajano.doxdb.DoxID;

public abstract class AbstractValidatingJsonDoxDAOBean extends AbstractJsonDoxDAOBean {

    @Override
    public DoxID create(JsonObject json,
            Principal principal) {

        validate(json);
        return super.create(json, principal);
    }

    /**
     * Extended by implementers to list the schema resources used for
     * validation. There must be at least one and the first one will be used to
     * validate the rest are added to provide extra definitions that may be
     * referenced by the first one. The list is expected to be names of
     * resources that are found on the application class path.
     *
     * @return list of resources.
     */
    protected abstract String[] getSchemaResources();

    @Override
    public void updateContent(DoxID doxId,
            JsonObject json,
            int version,
            Principal principal) {

        validate(json);
        super.updateContent(doxId, json, version, principal);

    }

    private void validate(JsonObject json) {

        // try {
        // final ValidationConfiguration cfg =
        // ValidationConfiguration.newBuilder()
        // .setDefaultVersion(SchemaVersion.DRAFTV4)
        // .freeze();
        // final LoadingConfiguration loadingCfg =
        // LoadingConfiguration.newBuilder().freeze();
        // final JsonValidator validator = JsonSchemaFactory.newBuilder()
        // .setLoadingConfiguration(loadingCfg)
        // .setValidationConfiguration(cfg)
        // .freeze()
        // .getValidator();
        //
        // validator.validate(null, JsonLoader.fromString(json.toString()))
        // .isSuccess();
        // } catch (final IOException | ProcessingException e) {
        // throw new PersistenceException(e);
        // }
    }

}
