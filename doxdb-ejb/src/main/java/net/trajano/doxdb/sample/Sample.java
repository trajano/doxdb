package net.trajano.doxdb.sample;

import javax.persistence.Entity;

import net.trajano.doxdb.DoxEntity;

@Entity
// @NamedQueries(@NamedQuery(name = "DoxEntity.readByDoxId", lockMode =
// LockModeType.OPTIMISTIC, query =
// "select e from DoxEntity e where e.doxId = :doxId"))
public class Sample extends DoxEntity {

}
