package net.trajano.doxdb.sample.test;

import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;

import net.trajano.commons.testing.ResourceUtil;

/**
 * This will call the REST API and load MOCK_DATA.json.
 *
 * @author Archimedes Trajano
 */
public class DataLoader {

    public static void main(final String[] args) {

        final InputStream resourceAsStream = ResourceUtil.getResourceAsStream("MOCK_DATA.json");
        final JsonArray collection = Json.createReader(resourceAsStream).readArray();
        final Builder request = ClientBuilder.newClient().target("http://localhost:8080/doxdb-sample/V1/venue").request(MediaType.APPLICATION_JSON_TYPE);

        for (final JsonValue item : collection) {
            final JsonObject record = (JsonObject) item;
            final JsonObject jsonObject = Json.createObjectBuilder().add("name", record.get("last_name")).add("language", record.get("language")).add("rings", Json.createArrayBuilder()).build();

            request.post(Entity.entity(jsonObject, MediaType.APPLICATION_JSON_TYPE));
        }

    }
}
