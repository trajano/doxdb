package net.trajano.doxdb.ext;

import com.github.fge.jsonschema.main.JsonSchema;

import net.trajano.doxdb.schema.CollectionType;
import net.trajano.doxdb.schema.DoxPersistence;
import net.trajano.doxdb.schema.SchemaType;

public interface ConfigurationProvider {

    /**
     * @param schemaName
     * @return
     */
    SchemaType getCollectionSchema(String schemaName);

    /**
     * @param location
     * @return
     */
    JsonSchema getContentSchema(String location);

    /**
     * @param collectionName
     *            collection name
     * @return
     */
    CollectionType getCollection(String collectionName);

    /**
     * Gets the mapped index name for a logical name.
     *
     * @param name
     *            logical name
     * @return physical name
     */
    String getMappedIndex(String name);

    DoxPersistence getPersistenceConfig();
}
