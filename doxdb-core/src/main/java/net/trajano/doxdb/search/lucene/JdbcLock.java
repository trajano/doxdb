package net.trajano.doxdb.search.lucene;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.lucene.store.Lock;

class JdbcLock extends Lock {

    /**
     * Wait this long to obtain the lock.
     */
    private static final int TIMEOUT_SECONDS = 5;

    private final Connection connection;

    private final String lockFileSql;

    private final String name;

    public JdbcLock(final String name, final Connection connection, final String searchTableName) {
        this.name = name;
        this.connection = connection;
        lockFileSql = String.format("select name from %1$s where name = ? for update", searchTableName);
    }

    /**
     * Does nothing.
     */
    @Override
    public void close() throws IOException {

    }

    /**
     *
     */
    @Override
    public boolean isLocked() throws IOException {

        return obtain();
    }

    @Override
    public boolean obtain() throws IOException {

        try (final PreparedStatement s = connection.prepareStatement(lockFileSql)) {
            s.setString(1, name);
            s.setQueryTimeout(TIMEOUT_SECONDS);
            try (ResultSet rs = s.executeQuery()) {
                rs.next();
            }
            return true;
        } catch (final SQLException e) {
            return false;
        }
    }

}