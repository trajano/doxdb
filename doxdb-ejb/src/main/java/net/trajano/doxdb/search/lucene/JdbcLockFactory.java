package net.trajano.doxdb.search.lucene;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.PersistenceException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;

import net.trajano.doxdb.ejb.internal.SqlConstants;

public class JdbcLockFactory extends LockFactory {

    /**
     * Lock.
     */
    private class SingleLock extends Lock {

        private final String lockName;

        public SingleLock(final String lockName) throws SQLException {

            this.lockName = lockName;

            final boolean hasLock;
            try (final PreparedStatement s = connection.prepareStatement(String.format(SqlConstants.SEARCHCHECKLOCK, searchTableName))) {
                s.setString(1, lockName);
                try (ResultSet rs = s.executeQuery()) {
                    hasLock = rs.next();
                }
            }
            if (!hasLock) {
                try (final PreparedStatement s = connection.prepareStatement(String.format(SqlConstants.SEARCHCREATELOCK, searchTableName))) {
                    s.setString(1, lockName);
                    s.setBoolean(2, false);
                    s.executeUpdate();
                }
            }
        }

        @Override
        public void close() throws IOException {

            try (final PreparedStatement s = connection.prepareStatement(String.format(SqlConstants.SEARCHUPDATELOCK, searchTableName))) {
                s.setBoolean(1, false);
                s.setString(2, lockName);
                s.setBoolean(3, true);
                s.executeUpdate();
            } catch (final SQLException e) {
                throw new IOException(e);
            }
        }

        @Override
        public boolean isLocked() throws IOException {

            try (final PreparedStatement s = connection.prepareStatement(String.format(SqlConstants.SEARCHCHECKLOCK, searchTableName))) {
                s.setString(1, lockName);
                try (ResultSet rs = s.executeQuery()) {
                    rs.next();
                    return rs.getBoolean(1);
                }
            } catch (final SQLException e) {
                throw new IOException(e);
            }
        }

        @Override
        public boolean obtain() throws IOException {

            try (final PreparedStatement s = connection.prepareStatement(String.format(SqlConstants.SEARCHUPDATELOCK, searchTableName))) {
                s.setBoolean(1, true);
                s.setString(2, lockName);
                s.setBoolean(3, false);
                return s.executeUpdate() == 1;
            } catch (final SQLException e) {
                throw new IOException(e);
            }
        }

    }

    private final Connection connection;

    private final String searchTableName;

    /**
     * Constructs JdbcLockFactory.
     *
     * @param connection
     *            connection
     * @param searchTableName
     *            search table name (in upper case)
     */
    public JdbcLockFactory(final Connection connection,
        final String searchTableName) {
        this.connection = connection;
        this.searchTableName = searchTableName;
    }

    @Override
    public Lock makeLock(final Directory dir,
        final String lockName) {

        try {
            return new SingleLock(lockName);
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

}
