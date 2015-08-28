package net.trajano.doxdb.sample.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.Callable;

import org.junit.Test;

import net.trajano.commons.testing.EqualsTestUtil;
import net.trajano.doxdb.DoxID;

public class IDTest {

    @Test
    public void testEqualsHashCodeToString() throws Exception {

        final DoxID generated = DoxID.generate();
        final DoxID rebuilt = new DoxID(generated.toString());
        assertEquals(generated, rebuilt);
        assertEquals(generated.hashCode(), rebuilt.hashCode());
    }

    @Test
    public void testEqualsHashCodeToStringUtility() throws Exception {

        final DoxID generated = DoxID.generate();
        EqualsTestUtil.assertEqualsImplementedCorrectly(new Callable<DoxID>() {

            @Override
            public DoxID call() throws Exception {

                return generated;
            }
        });
        EqualsTestUtil.assertEqualsImplementedCorrectly(new Callable<DoxID>() {

            @Override
            public DoxID call() throws Exception {

                return generated;
            }
        });
        final DoxID rebuilt = new DoxID(generated.toString());
        assertEquals(generated, rebuilt);
        assertEquals(generated.hashCode(), rebuilt.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCharacter() throws Exception {

        new DoxID("0123456789012345678901234567890\0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLength() throws Exception {

        new DoxID("01234567890123");
    }

    @Test
    public void testRandomness() throws Exception {

        final DoxID generated = DoxID.generate();
        assertFalse(generated.equals(DoxID.generate()));
        assertFalse(generated.equals(DoxID.generate()));
        assertFalse(generated.equals(DoxID.generate()));
        assertFalse(generated.equals(DoxID.generate()));
        assertFalse(generated.equals(DoxID.generate()));
    }

}
