package net.trajano.doxdb.search.lucene;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.PersistenceException;

import org.apache.lucene.store.BufferedIndexInput;
import org.apache.lucene.store.IOContext;

public class JdbcIndexInput extends BufferedIndexInput {

    private final ByteBuffer buffer;

    private int pos;

    private final ResultSet rs;

    private final PreparedStatement statement;

    protected JdbcIndexInput(String name, Connection connection, String searchTableName, IOContext context) {
        super(name, context);
        try {
            final String readSql = String.format("select content, contentlength from %1$s where name = ?", searchTableName);
            statement = connection.prepareStatement(readSql);
            statement.setString(1, name);
            rs = statement.executeQuery();
            rs.next();
            buffer = ByteBuffer.wrap(rs.getBytes(1));

            pos = 0;
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void close() throws IOException {

        try {
            rs.close();
            statement.close();
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public long length() {

        return buffer.limit();
    }

    @Override
    protected void readInternal(byte[] b,
            int offset,
            int length) throws IOException {

        System.arraycopy(buffer.array(), pos, b, offset, length);
    }

    @Override
    protected void seekInternal(long pos) throws IOException {

        this.pos = (int) pos;

    }

}
