package net.trajano.doxdb.sample.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import net.trajano.commons.testing.ResourceUtil;

/**
 * Load MOCK_DATA.json and testdata.csv into the database using the REST API.
 *
 * @author Archimedes Trajano
 */
public class DataLoader {

    public static void main(final String[] args) {

        try {
            final InputStream resourceAsStream = ResourceUtil.getResourceAsStream("MOCK_DATA.json");
            final JsonArray collection = Json.createReader(resourceAsStream).readArray();
            final WebTarget target = ClientBuilder.newClient().target("http://localhost:8080/doxdb-sample/V1");
            final Builder request = target.path("venue").request(MediaType.APPLICATION_JSON_TYPE);

            final long start = System.currentTimeMillis();
            for (final JsonValue item : collection) {
                final JsonObject record = (JsonObject) item;
                final JsonObject jsonObject = Json.createObjectBuilder()
                    .add("name", record.get("last_name"))
                    .add("feiId", String.valueOf(record.get("id")))
                    .add("language", record.get("language")).add("rings", Json.createArrayBuilder()).build();

                request.post(Entity.entity(jsonObject, MediaType.APPLICATION_JSON_TYPE)).readEntity(JsonObject.class);
            }

            final Builder request2 = target.path("horse").request(MediaType.APPLICATION_JSON_TYPE);
            final BufferedReader testDataStream = new BufferedReader(new InputStreamReader(ResourceUtil.getResourceAsStream("testdata.csv")));
            final String[] fieldNames = testDataStream.readLine().split("\\t");
            String line = testDataStream.readLine();
            while (line != null) {
                final Map<String, String> record = new HashMap<>(fieldNames.length);
                int i = 0;
                for (final String field : line.split("\\t")) {
                    record.put(fieldNames[i], field);
                    ++i;
                }

                final JsonObject jsonObject = Json.createObjectBuilder()
                    .add("name", record.get("Product Name"))
                    .add("countryOfBirth", record.get("Country"))
                    .add("fei", String.valueOf(record.get("Phone Number")))
                    .add("gender", "F".equals(record.get("Gender")) ? "mare" : "gelding")
                    .build();
                request2.post(Entity.entity(jsonObject, MediaType.APPLICATION_JSON_TYPE)).readEntity(JsonObject.class);

                line = testDataStream.readLine();
            }
            System.out.println(System.currentTimeMillis() - start);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
