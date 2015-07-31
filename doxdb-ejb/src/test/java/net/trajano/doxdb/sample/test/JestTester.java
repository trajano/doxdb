package net.trajano.doxdb.sample.test;

import javax.persistence.PersistenceException;

import org.junit.Test;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.indices.CreateIndex;

/**
 * type.
 *
 * @author Archimedes Trajano
 */
public class JestTester {

    @Test
    public void foo() throws Exception {

        final JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder("http://localhost:9200")
            .multiThreaded(true)
            .build());
        final JestClient client = factory.getObject();
        final JestResult execute = client.execute(new CreateIndex.Builder("FOODEX").build());
        System.out.println(execute.getJsonString());
        if (!execute.isSucceeded()) {
            throw new PersistenceException("FAIL");
        }

    }
}
