package net.trajano.doxdb.search.lucene;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;

public class JdbcDirectory extends Directory {

    private final DataSource ds;

    /**
     * May be passed in the future.
     */
    private final String searchTableName;

    public JdbcDirectory(final DataSource ds,
        final String searchTableName) throws SQLException {
        this.ds = ds;
        this.searchTableName = searchTableName.toUpperCase();
        createSearchTable();

    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public IndexOutput createOutput(final String name,
        final IOContext context) throws IOException {

        try (Connection connection = ds.getConnection()) {
            return new JdbcIndexOutput(name, connection, searchTableName);
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Creates the search tables if required.
     */
    private void createSearchTable() throws SQLException {

        try (Connection connection = ds.getConnection()) {
            try (final ResultSet tables = connection.getMetaData()
                .getTables(null, null, searchTableName, null)) {
                if (!tables.next()) {

                    final int lobSize = 1024 * 1024 * 1024;
                    try (final PreparedStatement s = connection.prepareStatement(String.format("CREATE TABLE %1$s (NAME VARCHAR(256), CONTENT BLOB(%2$d) NOT NULL, CONTENTLENGTH BIGINT, PRIMARY KEY (NAME))", searchTableName, lobSize))) {
                        s.executeUpdate();
                    }
                }
            }
        }
    }

    @Override
    public void deleteFile(final String name) throws IOException {

        try (Connection connection = ds.getConnection()) {
            final String deleteFileSql = String.format("delete from %1$s where name = ?", searchTableName);
            try (final PreparedStatement s = connection.prepareStatement(deleteFileSql)) {
                s.setString(1, name);
                final int count = s.executeUpdate();
                if (count != 1) {
                    throw new PersistenceException("expected a modification but didn't get any.");
                }
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public long fileLength(final String name) throws IOException {

        try (Connection connection = ds.getConnection()) {
            final String fileLengthSql = String.format("select contentlength from %1$s where name = ?", searchTableName);
            try (PreparedStatement s = connection.prepareStatement(fileLengthSql)) {
                s.setString(1, name);
                try (ResultSet rs = s.executeQuery()) {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public String[] listAll() throws IOException {

        try (Connection connection = ds.getConnection()) {

            final List<String> all = new LinkedList<>();
            final String selectFileSql = String.format("select name from %1$s", searchTableName);
            try (PreparedStatement s = connection.prepareStatement(selectFileSql)) {
                try (ResultSet rs = s.executeQuery()) {
                    while (rs.next()) {
                        all.add(rs.getString(1));
                    }
                    return all.toArray(new String[0]);
                }
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Creates a new lock. It does not store the lock in a map as that would
     * force this to be a singleton.
     */
    @Override
    public Lock makeLock(final String name) {

        System.out.println("locking ... " + name);
        try (Connection connection = ds.getConnection()) {
            return new JdbcLock(name, connection, searchTableName);
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public IndexInput openInput(final String name,
        final IOContext context) throws IOException {

        try (Connection connection = ds.getConnection()) {
            return new JdbcIndexInput(name, connection, searchTableName, context);
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void renameFile(final String source,
        final String dest) throws IOException {

        try (Connection connection = ds.getConnection()) {
            final String renameFileSql = String.format("update %1$s set name = ? where name = ?", searchTableName);
            try (final PreparedStatement s = connection.prepareStatement(renameFileSql)) {
                s.setString(1, dest);
                s.setString(2, source);
                final int count = s.executeUpdate();
                if (count != 1) {
                    throw new PersistenceException("expected a modification but didn't get any.");
                }
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }

    }

    /**
     * Does nothing, will use transaction from the container to commit the data.
     */
    @Override
    public void sync(final Collection<String> names) throws IOException {

    }

}
