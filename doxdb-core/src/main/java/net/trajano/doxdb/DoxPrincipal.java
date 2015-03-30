package net.trajano.doxdb;

import java.security.Principal;
import java.util.Objects;

public final class DoxPrincipal implements Principal {

    /**
     * Size of the ID in bytes.
     */
    public static final int LENGTH = 128;

    private final String principalName;

    public DoxPrincipal(String principalName) {

        this.principalName = principalName;
    }

    public DoxPrincipal(Principal principal) {

        this.principalName = principal.getName();
    }

    @Override
    public int hashCode() {

        return principalName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        DoxPrincipal other = (DoxPrincipal) obj;
        return Objects.equals(other.principalName, principalName);
    }

    @Override
    public String toString() {

        return principalName;
    }

    @Override
    public String getName() {

        return principalName;
    }

}
