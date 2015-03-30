package net.trajano.doxb.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.trajano.doxdb.internal.DoxPrincipal;

public class PrincipalTest {

    @Test
    public void testEqualsHashCodePrincipalConstructor() throws Exception {

        final DoxPrincipal p1 = new DoxPrincipal("ARCH");
        final DoxPrincipal p2 = new DoxPrincipal(p1);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testEqualsHashCodeToString() throws Exception {

        final DoxPrincipal p1 = new DoxPrincipal("ARCH");
        final DoxPrincipal p2 = new DoxPrincipal("ARCH");
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

}
