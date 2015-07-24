package net.trajano.doxdb.sampleejb;

import java.security.Principal;

import javax.ejb.Stateless;

import net.trajano.doxdb.CollectionAccessControl;

@Stateless
public class AccessControls implements
    CollectionAccessControl {

    @Override
    public boolean allowedCreate(final String storedJson,
        final Principal principal) {

        return true;
    }

    @Override
    public byte[] buildAccessKey(final String storedJson,
        final Principal principal) {

        return "HELLO".getBytes();
    }
}
