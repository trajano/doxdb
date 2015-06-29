package net.trajano.doxdb.sample;

import javax.ejb.Stateless;

import net.trajano.doxdb.search.AbstractLuceneDoxSearchBean;

@Stateless
// @NamedQueries(@NamedQuery(name = "DoxEntity.readByDoxId", lockMode =
// LockModeType.OPTIMISTIC, query =
// "select e from DoxEntity e where e.doxId = :doxId"))
public class SampleIndexer extends AbstractLuceneDoxSearchBean {

    @Override
    protected String getSearchTableName() {

        return "doxsearch";
    }

}
