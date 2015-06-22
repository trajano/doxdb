package net.trajano.doxdb.sample;

import java.sql.Connection;

import javax.ejb.Stateless;

import net.trajano.doxdb.AbstractDoxDAOBean;
import net.trajano.doxdb.DoxConfiguration;

@Stateless
// @NamedQueries(@NamedQuery(name = "DoxEntity.readByDoxId", lockMode =
// LockModeType.OPTIMISTIC, query =
// "select e from DoxEntity e where e.doxId = :doxId"))
public class SampleBean extends AbstractDoxDAOBean {

    public SampleBean() {
    }

    public SampleBean(Connection c) {
        super(c);
    }

    @Override
    protected DoxConfiguration buildConfiguration() {

        DoxConfiguration doxConfiguration = new DoxConfiguration();
        doxConfiguration.setHasOob(true);
        doxConfiguration.setTableName("DoxdbSample");
        return doxConfiguration;
    }

}
