package net.trajano.doxdb.sample.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.trajano.doxdb.sample.json.Country;
import net.trajano.doxdb.sample.json.Horse;
import net.trajano.doxdb.sample.json.Horse.Gender;

public class JsonBindingTest {

    @Test
    public void testJson() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final Horse horse = new Horse();
        horse.withName("archie")
            .withGender(Gender.GELDING)
            .withCountryOfBirth(Country.PH);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        mapper.writeValue(baos, horse);
        baos.close();

        final Horse readValue = mapper.readValue(baos.toByteArray(), Horse.class);
        assertEquals(horse, readValue);
    }
}
