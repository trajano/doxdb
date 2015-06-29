package net.trajano.doxdb.sample;

import javax.ejb.Stateless;

import net.trajano.doxdb.DoxConfiguration;
import net.trajano.doxdb.json.AbstractValidatingJsonDoxDAOBean;

@Stateless
// @NamedQueries(@NamedQuery(name = "DoxEntity.readByDoxId", lockMode =
// LockModeType.OPTIMISTIC, query =
// "select e from DoxEntity e where e.doxId = :doxId"))
public class SampleJsonBean extends AbstractValidatingJsonDoxDAOBean {

    @Override
    protected DoxConfiguration buildConfiguration() {

        DoxConfiguration doxConfiguration = new DoxConfiguration();
        doxConfiguration.setHasOob(true);
        doxConfiguration.setTableName("JsonSample");
        return doxConfiguration;
    }

    @Override
    protected String getSchemaResource() {

        return "/schema/horse.json";
    }

}
