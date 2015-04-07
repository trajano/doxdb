package net.trajano.doxdb;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

public class DoxDAO {

    private final Connection c;

    private final String deleteSql;

    private final String insertSql;

    private final String readContentSql;

    private final String readSql;

    private final String readForUpdateSql;

    private final String tableName;

    private final String updateSql;

    private final String copyToTombstoneSql;

    private final String oobDeleteSql;

    private final String oobInsertSql;

    private final String oobReadContentSql;

    private final String oobReadSql;

    private final String oobReadForUpdateSql;

    private final String oobUpdateSql;

    private boolean hasOob;

    public DoxDAO(final Connection c, final String tableName) {

        this(c, new DoxConfiguration(tableName));
    }

    public DoxDAO(final Connection c, final DoxConfiguration configuration) {

        this.c = c;
        this.tableName = configuration.getTableName();
        this.hasOob = configuration.isHasOob();
        try {
            createTable();
            insertSql = "insert into " + tableName + " (CONTENT, DOXID, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, VERSION) values (?, ?,?,?,?,?,?)";
            oobInsertSql = "insert into " + tableName + "OOB (CONTENT, DOXID, PARENTID, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON) values (?,?,?,?,?,?,?,?)";

            readSql = "select ID, DOXID, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, VERSION from " + tableName + " E where E.DOXID=?";
            oobReadSql = "select ID, DOXID, PARENTID, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, VERSION from " + tableName + "OOB E where E.DOXID=? and E.REFERENCE = ?";

            readForUpdateSql = "select ID, DOXID, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, VERSION from " + tableName + " E where E.DOXID=? AND E.VERSION = ? FOR UPDATE";
            // when reading for update on the OOB data it will lock all the OOB
            // records not just the one referenced by the name.
            oobReadForUpdateSql = "select ID, DOXID, PARENTID, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, VERSION from " + tableName + "OOB E where E.DOXID=? FOR UPDATE";

            readContentSql = "select E.CONTENT from " + tableName + " E where E.ID=?";
            oobReadContentSql = "select E.CONTENT from " + tableName + "OOB E where E.ID=?";

            updateSql = "update " + tableName + " set CONTENT=?, LASTUPDATEDBY=?, LASTUPDATEDON=?, VERSION=VERSION+1 where ID=? and VERSION=?";
            oobUpdateSql = "update " + tableName + "OOB set CONTENT=?, LASTUPDATEDBY=?, LASTUPDATEDON=?, VERSION=VERSION+1 where ID=? and VERSION=?";

            copyToTombstoneSql = "insert into " + tableName + "TOMBSTONE (CONTENT, DOXID, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, DELETEDBY, DELETEDON) select CONTENT, DOXID, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, ?, ? from " + tableName + " where id = ? and version = ?";
            deleteSql = "delete from " + tableName + " where ID=? and VERSION=?";
            oobDeleteSql = "delete from " + tableName + "OOB where ID=? and VERSION=?";
            for (final String sql : new String[] { insertSql, readSql, readContentSql, updateSql, deleteSql, readForUpdateSql }) {
                c.prepareStatement(sql)
                        .close();
            }

            if (hasOob) {
                for (final String sql : new String[] { oobInsertSql, oobReadSql, oobReadContentSql, oobUpdateSql, oobDeleteSql, oobReadForUpdateSql }) {
                    c.prepareStatement(sql)
                            .close();
                }
            }

        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public DoxID create(final InputStream in,
            DoxPrincipal principal) {

        try {
            final DoxID doxId = DoxID.generate();
            PreparedStatement s = c.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            Timestamp ts = new Timestamp(System.currentTimeMillis());
            s.setBinaryStream(1, in);
            s.setString(2, doxId.toString());
            s.setString(3, principal.getName());
            s.setTimestamp(4, ts);
            s.setString(5, principal.getName());
            s.setTimestamp(6, ts);
            s.setInt(7, 1);
            s.executeUpdate();
            ResultSet rs = s.getGeneratedKeys();
            rs.next();
            return doxId;
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public int getVersion(DoxID id) {

        return readMeta(id).getVersion();
    }

    /**
     * Deletes make a copy of the current record to their respective tombstone
     * table
     * 
     * @param id
     * @param version
     */
    public void delete(DoxID id,
            int version,
            DoxPrincipal principal) {

        try {
            DocumentMeta meta = readMetaForLock(id, version);
            Timestamp ts = new Timestamp(System.currentTimeMillis());
            PreparedStatement s = c.prepareStatement(copyToTombstoneSql);
            s.setString(1, principal.toString());
            s.setTimestamp(2, ts);
            s.setLong(3, meta.getId());
            s.setInt(4, meta.getVersion());
            s.executeUpdate();

            PreparedStatement t = c.prepareStatement(deleteSql);
            t.setLong(1, meta.getId());
            t.setInt(2, meta.getVersion());
            t.executeUpdate();
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public DocumentMeta readMeta(DoxID id) {

        try {
            PreparedStatement s = c.prepareStatement(readSql);
            s.setString(1, id.toString());
            ResultSet rs = s.executeQuery();
            if (!rs.next()) {
                throw new EntityNotFoundException();
            }
            DocumentMeta meta = new DocumentMeta();
            meta.setId(rs.getLong(1));
            meta.setDoxId(new DoxID(rs.getString(2)));
            meta.setCreatedBy(new DoxPrincipal(rs.getString(3)));
            meta.setCreatedOn(rs.getTimestamp(4));
            meta.setLastUpdatedBy(new DoxPrincipal(rs.getString(5)));
            meta.setLastUpdatedOn(rs.getTimestamp(6));
            meta.setVersion(rs.getInt(7));
            return meta;
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    private DocumentMeta readMetaForLock(DoxID id,
            int version) throws SQLException {

        PreparedStatement s = c.prepareStatement(readForUpdateSql);
        s.setString(1, id.toString());
        s.setInt(2, version);
        ResultSet rs = s.executeQuery();
        if (!rs.next()) {
            throw new OptimisticLockException();
        }
        DocumentMeta meta = new DocumentMeta();
        meta.setId(rs.getLong(1));
        meta.setDoxId(new DoxID(rs.getString(2)));
        meta.setCreatedBy(new DoxPrincipal(rs.getString(3)));
        meta.setCreatedOn(rs.getTimestamp(4));
        meta.setLastUpdatedBy(new DoxPrincipal(rs.getString(5)));
        meta.setLastUpdatedOn(rs.getTimestamp(6));
        meta.setVersion(rs.getInt(7));
        return meta;
    }

    public InputStream readContent(DoxID id) {

        try {
            PreparedStatement s = c.prepareStatement(readContentSql);
            s.setLong(1, readMeta(id).getId());
            ResultSet rs = s.executeQuery();
            if (!rs.next()) {
                throw new EntityNotFoundException();
            }
            return rs.getBinaryStream(1);
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public void createTable() {

        try {
            if (!c.getMetaData()
                    .getTables(null, null, tableName, null)
                    .next()) {
                c.prepareStatement("CREATE TABLE " + tableName + "(ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONTENT BLOB(2147483647) NOT NULL, CREATEDBY VARCHAR(128) NOT NULL, CREATEDON TIMESTAMP NOT NULL, DOXID VARCHAR(32) NOT NULL, LASTUPDATEDBY VARCHAR(128) NOT NULL, LASTUPDATEDON TIMESTAMP NOT NULL, VERSION INTEGER, PRIMARY KEY (ID))")
                        .executeUpdate();
                c.prepareStatement("ALTER TABLE " + tableName + " add unique (DOXID)")
                        .executeUpdate();
                c.prepareStatement("CREATE TABLE " + tableName + "TOMBSTONE (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONTENT BLOB(2147483647) NOT NULL, CREATEDBY VARCHAR(128) NOT NULL, CREATEDON TIMESTAMP NOT NULL, DOXID VARCHAR(32) NOT NULL, LASTUPDATEDBY VARCHAR(128) NOT NULL, LASTUPDATEDON TIMESTAMP NOT NULL, DELETEDBY VARCHAR(128) NOT NULL, DELETEDON TIMESTAMP NOT NULL, PRIMARY KEY (ID))")
                        .executeUpdate();
                c.prepareStatement("ALTER TABLE " + tableName + "TOMBSTONE  add unique (DOXID)")
                        .executeUpdate();
            }
            // An OOB table would have a reference label for the parent record
            // but it needs to be unique. However in the tombstone it does not
            // need to be unique. Also on the tombstone it does not reference
            // the record by the primary key because the record may not have
            // been deleted on the primary key level but the OOB data has been
            // removed.
            if (hasOob && !c.getMetaData()
                    .getTables(null, null, tableName, null)
                    .next()) {
                c.prepareStatement("CREATE TABLE " + tableName + "OOB (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, PARENTID BIGINT NOT NULL, CONTENT BLOB(2147483647) NOT NULL, REFERENCE VARCHAR(128) NOT NULL, CREATEDBY VARCHAR(128) NOT NULL, CREATEDON TIMESTAMP NOT NULL, DOXID VARCHAR(32) NOT NULL, LASTUPDATEDBY VARCHAR(128) NOT NULL, LASTUPDATEDON TIMESTAMP NOT NULL, VERSION INTEGER, PRIMARY KEY (ID))")
                        .executeUpdate();
                c.prepareStatement("ALTER TABLE " + tableName + "OOB add unique (DOXID, REFERENCE)")
                        .executeUpdate();
                c.prepareStatement("ALTER TABLE " + tableName + "OOB add unique (PARENTID, REFERENCE)")
                        .executeUpdate();
                c.prepareStatement("ALTER TABLE " + tableName + "OOB add foreign key (PARENTID) references " + tableName + "(ID)")
                        .executeUpdate();
                c.prepareStatement("CREATE TABLE " + tableName + "OOBTOMBSTONE (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, PARENTDOXID BIGINT NOT NULL, CONTENT BLOB(2147483647) NOT NULL, REFERENCE VARCHAR(128) NOT NULL, CREATEDBY VARCHAR(128) NOT NULL, CREATEDON TIMESTAMP NOT NULL, DOXID VARCHAR(32) NOT NULL, LASTUPDATEDBY VARCHAR(128) NOT NULL, LASTUPDATEDON TIMESTAMP NOT NULL, DELETEDBY VARCHAR(128) NOT NULL, DELETEDON TIMESTAMP NOT NULL, PRIMARY KEY (ID))")
                        .executeUpdate();
                c.prepareStatement("ALTER TABLE " + tableName + "OOBTOMBSTONE  add unique (DOXID, REFERENCE)")
                        .executeUpdate();
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }

    }
}
