package net.trajano.doxdb;

import java.security.Principal;
import java.util.Objects;

public final class DoxPrincipal implements Principal {

    /**
     * Size of the ID in bytes.
     */
    public static final int LENGTH = 128;

    private final String principalName;

    public DoxPrincipal(final Principal principal) {

        principalName = principal.getName();
    }

    public DoxPrincipal(final String principalName) {

        this.principalName = principalName;
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        final DoxPrincipal other = (DoxPrincipal) obj;
        return Objects.equals(other.principalName, principalName);
    }

    @Override
    public String getName() {

        return principalName;
    }

    @Override
    public int hashCode() {

        return principalName.hashCode();
    }

    @Override
    public String toString() {

        return principalName;
    }

}
