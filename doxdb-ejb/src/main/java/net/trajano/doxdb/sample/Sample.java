package net.trajano.doxdb.sample;

import java.sql.Connection;

import javax.ejb.Stateless;

import net.trajano.doxdb.DoxConfiguration;
import net.trajano.doxdb.jdbc.JdbcDoxDAO;

@Stateless
// @NamedQueries(@NamedQuery(name = "DoxEntity.readByDoxId", lockMode =
// LockModeType.OPTIMISTIC, query =
// "select e from DoxEntity e where e.doxId = :doxId"))
public class Sample extends JdbcDoxDAO {

    public Sample(Connection c, DoxConfiguration configuration) {

        super(c, configuration);
        // TODO Auto-generated constructor stub
    }

}
