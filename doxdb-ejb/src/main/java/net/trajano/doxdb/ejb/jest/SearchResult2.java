package net.trajano.doxdb.ejb.jest;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.searchbox.core.SearchResult;

/**
 * modifies search result so it can have the collection and ID data as well
 *
 * @author Archimedes Trajano
 */
public class SearchResult2 extends SearchResult {

    public static final String ES_METADATA_INDEX = "_index";

    public static final String ES_METADATA_TYPE = "_collection";

    /**
     * Constructs SearchResult2.
     *
     * @param source
     */
    public SearchResult2(final SearchResult source) {
        super(source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<JsonElement> extractSource() {

        final List<JsonElement> sourceList = new ArrayList<JsonElement>();

        if (jsonObject != null) {
            final String[] keys = getKeys();
            if (keys == null) {
                sourceList.add(jsonObject);
            } else {
                final String sourceKey = keys[keys.length - 1];
                JsonElement obj = jsonObject.get(keys[0]);
                if (keys.length > 1) {
                    for (int i = 1; i < keys.length - 1; i++) {
                        obj = ((JsonObject) obj).get(keys[i]);
                    }

                    if (obj.isJsonObject()) {
                        final JsonElement source = obj.getAsJsonObject().get(sourceKey);
                        if (source != null) {
                            sourceList.add(source);
                        }
                    } else if (obj.isJsonArray()) {
                        for (final JsonElement element : obj.getAsJsonArray()) {
                            if (element instanceof JsonObject) {
                                final JsonObject currentObj = element.getAsJsonObject();
                                final JsonObject source = currentObj.getAsJsonObject(sourceKey);
                                if (source != null) {
                                    source.add(ES_METADATA_ID, currentObj.get("_id"));
                                    source.add(ES_METADATA_INDEX, currentObj.get("_index"));
                                    source.add(ES_METADATA_TYPE, currentObj.get("_type"));
                                    sourceList.add(source);
                                }
                            }
                        }
                    }
                } else if (obj != null) {
                    final JsonElement objId = jsonObject.get("_id");
                    if (objId != null && obj.isJsonObject()) {
                        obj.getAsJsonObject().add(ES_METADATA_ID, objId);
                    }
                    obj.getAsJsonObject().add(ES_METADATA_INDEX, jsonObject.get("_index"));
                    obj.getAsJsonObject().add(ES_METADATA_TYPE, jsonObject.get("_type"));
                    sourceList.add(obj);
                }
            }
        }

        return sourceList;
    }
}
