package net.trajano.doxdb.internal;

import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;

public final class DoxPrincipal implements
    Principal,
    Serializable {

    /**
     * Size of the ID in bytes.
     */
    public static final int LENGTH = 128;

    /**
     * bare_field_name.
     */
    private static final long serialVersionUID = 5204441416021635252L;

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
