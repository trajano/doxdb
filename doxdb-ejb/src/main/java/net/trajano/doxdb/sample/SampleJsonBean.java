package net.trajano.doxdb.sample;

import java.sql.Connection;

import javax.ejb.Stateless;

import net.trajano.doxdb.DoxConfiguration;
import net.trajano.doxdb.json.AbstractJsonDoxDAOBean;

@Stateless
// @NamedQueries(@NamedQuery(name = "DoxEntity.readByDoxId", lockMode =
// LockModeType.OPTIMISTIC, query =
// "select e from DoxEntity e where e.doxId = :doxId"))
public class SampleJsonBean extends AbstractJsonDoxDAOBean {

    public SampleJsonBean() {
    }

    public SampleJsonBean(Connection c) {
        super(c);
    }

    @Override
    protected DoxConfiguration buildConfiguration() {

        DoxConfiguration doxConfiguration = new DoxConfiguration();
        doxConfiguration.setHasOob(true);
        doxConfiguration.setTableName("JsonSample");
        return doxConfiguration;
    }

}
