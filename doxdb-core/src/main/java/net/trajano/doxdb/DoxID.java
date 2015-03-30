package net.trajano.doxdb;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * An generated ID object. Unlike a UUID, it has less character restrictions. It
 * is a {@value #LENGTH} character random number string. Implementation wise it
 * uses a less secure but more performant {@link ThreadLocalRandom} generator to
 * generate new values.
 *
 * @author Archimedes
 */
public final class DoxID {

    /**
     * Size of the ID in bytes.
     */
    public static final int LENGTH = 32;

    /**
     * Set of allowed characters in the ID. Every printable US-ASCII character
     * except for space, quote, double quote and ampersand are allowed.
     */
    private static final char[] ALLOWED = "!#$%()*+,-./0123456789:;=?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~".toCharArray();

    /**
     * Generate a new instance with a randomized ID value.
     * 
     * @return
     */
    public static DoxID generate() {

        return new DoxID();

    }

    private final char[] b = new char[LENGTH];

    private final int hash;

    private DoxID() {

        final ThreadLocalRandom random = ThreadLocalRandom.current();
        int currentHash = LENGTH;
        for (int i = 0; i < LENGTH; ++i) {
            final int rand = random.nextInt(0, ALLOWED.length);
            b[i] = (char) ALLOWED[rand];
            currentHash += b[i];
        }
        hash = currentHash;

    }

    public DoxID(String s) {

        if (s.length() != LENGTH) {
            throw new IllegalArgumentException("input needs to be " + LENGTH + " in length.");
        }
        int currentHash = LENGTH;
        char[] chars = s.toCharArray();
        for (int i = 0; i < LENGTH; ++i) {
            b[i] = chars[i];
            currentHash += chars[i];
        }
        hash = currentHash;
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        final DoxID other = (DoxID) obj;
        return Arrays.equals(b, other.b);
    }

    @Override
    public int hashCode() {

        return hash;
    }

    /**
     * This is the string representation of the key. This is normally used for
     * persisting.
     */
    @Override
    public String toString() {

        return new String(b);
    }
}
