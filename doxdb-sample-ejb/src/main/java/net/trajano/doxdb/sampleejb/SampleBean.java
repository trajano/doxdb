package net.trajano.doxdb.sampleejb;

import javax.ejb.Stateless;

import net.trajano.doxdb.AbstractStreamDoxDAOBean;
import net.trajano.doxdb.DoxConfiguration;

@Stateless
// @NamedQueries(@NamedQuery(name = "DoxEntity.readByDoxId", lockMode =
// LockModeType.OPTIMISTIC, query =
// "select e from DoxEntity e where e.doxId = :doxId"))
public class SampleBean extends AbstractStreamDoxDAOBean {

    @Override
    protected DoxConfiguration buildConfiguration() {

        final DoxConfiguration doxConfiguration = new DoxConfiguration();
        doxConfiguration.setHasOob(true);
        doxConfiguration.setTableName("XXDoxdbSample");
        return doxConfiguration;
    }

}
