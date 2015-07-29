package net.trajano.doxdb.search.lucene;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;

public class JpaLockFactory extends LockFactory {

    /**
     * Lock.
     */
    private class SingleLock extends Lock {

        public SingleLock() {
        }

        @Override
        public void close() throws IOException {

        }

        @Override
        public boolean isLocked() throws IOException {

            return false;
        }

        @Override
        public boolean obtain() throws IOException {

            em.createNamedQuery("searchListAll").setParameter("directoryName", directoryName).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
            return true;
        }

    }

    private final String directoryName;

    private final EntityManager em;

    /**
     * Constructs JpaLockFactory.
     *
     * @param em
     *            entity manager
     * @param directoryName
     *            index name.
     */
    public JpaLockFactory(final EntityManager em,
        final String directoryName) {
        this.em = em;
        this.directoryName = directoryName;
    }

    @Override
    public Lock makeLock(final Directory dir,
        final String lockName) {

        return new SingleLock();
    }

}
