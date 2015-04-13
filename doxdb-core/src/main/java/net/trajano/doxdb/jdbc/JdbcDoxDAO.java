package net.trajano.doxdb.jdbc;

import java.io.InputStream;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import net.trajano.doxdb.DoxConfiguration;
import net.trajano.doxdb.DoxDAO;
import net.trajano.doxdb.DoxID;

public class JdbcDoxDAO implements DoxDAO {

    private final Connection c;

    private final String copyToTombstoneSql;

    private final String deleteSql;

    private boolean hasOob;

    private final String insertSql;

    private final String oobCheckSql;

    private final String oobTombstoneDeleteSql;

    private final String oobDeleteSql;

    private final String oobInsertSql;

    private final String oobReadContentSql;

    private final String oobReadForUpdateSql;

    private final String oobReadSql;

    private final String oobUpdateSql;

    private final String readContentSql;

    private final String readForUpdateSql;

    private final String readSql;

    private final String tableName;

    private final String updateSql;

    private final String updateVersionSql;

    public JdbcDoxDAO(final Connection c, final DoxConfiguration configuration) {

        this.c = c;
        tableName = configuration.getTableName();
        hasOob = configuration.isHasOob();
        try {
            createTable();
            insertSql = "insert into " + tableName + " (CONTENT, DOXID, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, VERSION) values (?, ?,?,?,?,?,?)";
            oobInsertSql = "insert into " + tableName + "OOB (CONTENT, DOXID, PARENTID, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, VERSION) values (?,?,?,?,?,?,?,?,?)";

            readSql = "select ID, DOXID, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, VERSION from " + tableName + " E where E.DOXID=?";
            oobReadSql = "select ID, DOXID, PARENTID, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, VERSION from " + tableName + "OOB E where E.DOXID=? and E.REFERENCE = ?";
            oobCheckSql = String.format("select id, version from %s where parentid = ? and reference = ? for update", tableName + "OOB");
            oobTombstoneDeleteSql = String.format("delete from %s where doxid = ? and reference = ?", tableName + "OOBTOMBSTONE");
            readForUpdateSql = "select ID, DOXID, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, VERSION from " + tableName + " E where E.DOXID=? AND E.VERSION = ? FOR UPDATE";
            // when reading for update on the OOB data it will lock all the OOB
            // records not just the one referenced by the name.
            // perhaps in future versions we will optimistic lock the OOB data
            // but that would mean passing two version numbers
            oobReadForUpdateSql = "select ID, DOXID, PARENTID, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, VERSION from " + tableName + "OOB E where E.DOXID=? FOR UPDATE";

            readContentSql = "select E.CONTENT from " + tableName + " E where E.ID=?";
            oobReadContentSql = "select E.CONTENT from " + tableName + "OOB E where E.PARENTID=? and REFERENCE=?";

            updateSql = "update " + tableName + " set CONTENT=?, LASTUPDATEDBY=?, LASTUPDATEDON=?, VERSION=VERSION+1 where ID=? and VERSION=?";
            updateVersionSql = "update " + tableName + " set VERSION=VERSION+1 where ID=? and VERSION=?";
            oobUpdateSql = "update " + tableName + "OOB set CONTENT=?, LASTUPDATEDBY=?, LASTUPDATEDON=?, VERSION=VERSION+1 where ID=? and VERSION=?";

            copyToTombstoneSql = "insert into " + tableName + "TOMBSTONE (CONTENT, DOXID, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, DELETEDBY, DELETEDON) select CONTENT, DOXID, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, ?, ? from " + tableName + " where id = ? and version = ?";
            deleteSql = "delete from " + tableName + " where ID=? and VERSION=?";
            oobDeleteSql = "delete from " + tableName + "OOB where ID=? and VERSION=?";
            for (final String sql : new String[] { insertSql, readSql, readContentSql, updateSql, updateVersionSql, deleteSql, readForUpdateSql }) {
                c.prepareStatement(sql)
                        .close();
            }

            if (hasOob) {
                for (final String sql : new String[] { oobInsertSql, oobReadSql, oobReadContentSql, oobUpdateSql, oobDeleteSql, oobReadForUpdateSql, oobCheckSql, oobTombstoneDeleteSql }) {
                    c.prepareStatement(sql)
                            .close();
                }
            }

        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public JdbcDoxDAO(final Connection c, final String tableName) {

        this(c, new DoxConfiguration(tableName));
    }

    /**
     * Attaching will increment the doxid record version.
     *
     * @param doxId
     * @param reference
     * @param in
     * @param version
     * @param principal
     */
    @Override
    public void attach(final DoxID doxId,
            final String reference,
            final InputStream in,
            final int version,
            final Principal principal) {

        try {
            final DocumentMeta meta = readMetaAndLock(doxId, version);

            final PreparedStatement check = c.prepareStatement(oobCheckSql);
            check.setLong(1, meta.getId());
            check.setString(2, reference);

            final ResultSet checkRs = check.executeQuery();

            if (checkRs.next()) {
                // if the reference record already exists then do an update
                final long existingId = checkRs.getLong(1);
                final int existingVersion = checkRs.getInt(2);

                final PreparedStatement s = c.prepareStatement(oobUpdateSql);
                final Timestamp ts = new Timestamp(System.currentTimeMillis());
                s.setBinaryStream(1, in);
                s.setString(2, principal.getName());
                s.setTimestamp(3, ts);
                s.setLong(4, existingId);
                s.setInt(5, existingVersion);
                final int count = s.executeUpdate();
                if (count != 1) {
                    throw new PersistenceException("unable to update OOB");
                }
                incrementVersionNumber(meta.getId(), version);
                return;

            }

            // Delete any tombstone data if one exists
            final PreparedStatement checkTombStone = c.prepareStatement(oobTombstoneDeleteSql);
            checkTombStone.setString(1, doxId.toString());
            checkTombStone.setString(2, reference);
            checkTombStone.executeUpdate();

            // Insert a new OOB record

            final PreparedStatement s = c.prepareStatement(oobInsertSql, Statement.RETURN_GENERATED_KEYS);
            final Timestamp ts = new Timestamp(System.currentTimeMillis());
            s.setBinaryStream(1, in);
            s.setString(2, doxId.toString());
            s.setLong(3, meta.getId());
            s.setString(4, reference);
            s.setString(5, principal.getName());
            s.setTimestamp(6, ts);
            s.setString(7, principal.getName());
            s.setTimestamp(8, ts);
            s.setInt(9, version);
            s.executeUpdate();
            incrementVersionNumber(meta.getId(), version);
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }

    }

    @Override
    public DoxID create(final InputStream in,
            final Principal principal) {

        try {
            final DoxID doxId = DoxID.generate();

            final PreparedStatement s = c.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            final Timestamp ts = new Timestamp(System.currentTimeMillis());
            s.setBinaryStream(1, in);
            s.setString(2, doxId.toString());
            s.setString(3, principal.getName());
            s.setTimestamp(4, ts);
            s.setString(5, principal.getName());
            s.setTimestamp(6, ts);
            s.setInt(7, 1);
            s.executeUpdate();
            final ResultSet rs = s.getGeneratedKeys();
            rs.next();
            return doxId;
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    private void createOobTables() throws SQLException {

        c.prepareStatement("CREATE TABLE " + tableName + "OOB (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, PARENTID BIGINT NOT NULL, CONTENT BLOB(2147483647) NOT NULL, REFERENCE VARCHAR(128) NOT NULL, CREATEDBY VARCHAR(128) NOT NULL, CREATEDON TIMESTAMP NOT NULL, DOXID VARCHAR(32) NOT NULL, LASTUPDATEDBY VARCHAR(128) NOT NULL, LASTUPDATEDON TIMESTAMP NOT NULL, VERSION INTEGER NOT NULL, PRIMARY KEY (ID))")
                .executeUpdate();
        c.prepareStatement("ALTER TABLE " + tableName + "OOB add unique (DOXID, REFERENCE)")
                .executeUpdate();
        c.prepareStatement("ALTER TABLE " + tableName + "OOB add unique (PARENTID, REFERENCE)")
                .executeUpdate();
        c.prepareStatement("ALTER TABLE " + tableName + "OOB add foreign key (PARENTID, DOXID) references " + tableName + "(ID, DOXID)")
                .executeUpdate();
        c.prepareStatement("CREATE TABLE " + tableName + "OOBTOMBSTONE (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONTENT BLOB(2147483647) NOT NULL, REFERENCE VARCHAR(128) NOT NULL, CREATEDBY VARCHAR(128) NOT NULL, CREATEDON TIMESTAMP NOT NULL, DOXID VARCHAR(32) NOT NULL, LASTUPDATEDBY VARCHAR(128) NOT NULL, LASTUPDATEDON TIMESTAMP NOT NULL, DELETEDBY VARCHAR(128) NOT NULL, DELETEDON TIMESTAMP NOT NULL, PRIMARY KEY (ID))")
                .executeUpdate();
        c.prepareStatement("ALTER TABLE " + tableName + "OOBTOMBSTONE  add unique (DOXID, REFERENCE)")
                .executeUpdate();
    }

    public void createTable() {

        try {

            if (!c.getMetaData()
                    .getTables(null, null, tableName.toUpperCase(), null)
                    .next()) {
                c.prepareStatement("CREATE TABLE " + tableName + "(ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONTENT BLOB(2147483647) NOT NULL, CREATEDBY VARCHAR(128) NOT NULL, CREATEDON TIMESTAMP NOT NULL, DOXID VARCHAR(32) NOT NULL, LASTUPDATEDBY VARCHAR(128) NOT NULL, LASTUPDATEDON TIMESTAMP NOT NULL, VERSION INTEGER, PRIMARY KEY (ID))")
                        .executeUpdate();
                c.prepareStatement("ALTER TABLE " + tableName + " add unique (DOXID)")
                        .executeUpdate();
                c.prepareStatement("ALTER TABLE " + tableName + " add unique (ID, DOXID)")
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
                createOobTables();
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }

    }

    /**
     * Deletes make a copy of the current record to their respective tombstone
     * table
     *
     * @param id
     * @param version
     */
    @Override
    public void delete(final DoxID id,
            final int version,
            final Principal principal) {

        try {
            final DocumentMeta meta = readMetaAndLock(id, version);
            final Timestamp ts = new Timestamp(System.currentTimeMillis());

            if (hasOob) {
                final String oobCopyAllToTombstoneSql = "insert into " + tableName + "OOBTOMBSTONE (CONTENT, DOXID, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, DELETEDBY, DELETEDON) select CONTENT, DOXID, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, ?, ? from " + tableName + "OOB where parentid = ?";
                final PreparedStatement copy = c.prepareStatement(oobCopyAllToTombstoneSql);
                copy.setString(1, principal.toString());
                copy.setTimestamp(2, ts);
                copy.setLong(3, meta.getId());
                final int copyCount = copy.executeUpdate();

                final String oobDeleteAllSql = "delete from " + tableName + "OOB where PARENTID = ?";
                final PreparedStatement del = c.prepareStatement(oobDeleteAllSql);
                del.setLong(1, meta.getId());
                final int delCount = del.executeUpdate();
                if (copyCount != delCount) {
                    throw new PersistenceException("Mismatch in moving OOB to tombstone");
                }
            }

            // c.prepareStatement("CREATE TABLE " + tableName +
            // "OOB (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, PARENTID BIGINT NOT NULL, CONTENT BLOB(2147483647) NOT NULL, REFERENCE VARCHAR(128) NOT NULL, CREATEDBY VARCHAR(128) NOT NULL, CREATEDON TIMESTAMP NOT NULL, DOXID VARCHAR(32) NOT NULL, LASTUPDATEDBY VARCHAR(128) NOT NULL, LASTUPDATEDON TIMESTAMP NOT NULL, VERSION INTEGER NOT NULL, PRIMARY KEY (ID))")
            final PreparedStatement s = c.prepareStatement(copyToTombstoneSql);
            s.setString(1, principal.toString());
            s.setTimestamp(2, ts);
            s.setLong(3, meta.getId());
            s.setInt(4, meta.getVersion());
            s.executeUpdate();

            final PreparedStatement t = c.prepareStatement(deleteSql);
            t.setLong(1, meta.getId());
            t.setInt(2, meta.getVersion());
            final int deletedRows = t.executeUpdate();
            if (deletedRows != 1) {
                throw new PersistenceException("problem with the delete");
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Detaches an OOB data
     *
     * @param doxId
     * @param reference
     * @param version
     * @param principal
     */
    @Override
    public void detach(final DoxID doxId,
            final String reference,
            final int version,
            final Principal principal) {

        if (!hasOob) {
            throw new UnsupportedOperationException("OOB support is not present in " + tableName);
        }
        try {
            final DocumentMeta meta = readMetaAndLock(doxId, version);
            final Timestamp ts = new Timestamp(System.currentTimeMillis());

            final String oobCopyToTombstoneSql = "insert into " + tableName + "OOBTOMBSTONE (CONTENT, DOXID, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, DELETEDBY, DELETEDON) select CONTENT, DOXID, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, ?, ? from " + tableName + "OOB where parentid = ? and reference = ?";
            final PreparedStatement copy = c.prepareStatement(oobCopyToTombstoneSql);
            copy.setString(1, principal.toString());
            copy.setTimestamp(2, ts);
            copy.setLong(3, meta.getId());
            copy.setString(4, reference);
            final int copyCount = copy.executeUpdate();
            if (copyCount != 1) {
                throw new EntityNotFoundException();
            }

            final String oobDeleteSql = "delete from " + tableName + "OOB where PARENTID = ? AND REFERENCE = ?";
            final PreparedStatement del = c.prepareStatement(oobDeleteSql);
            del.setLong(1, meta.getId());
            del.setString(2, reference);
            final int delCount = del.executeUpdate();
            if (copyCount != delCount) {
                throw new PersistenceException("Mismatch in moving OOB to tombstone");
            }
            incrementVersionNumber(meta.getId(), version);

        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }

    }

    @Override
    public int getVersion(final DoxID id) {

        return readMeta(id).getVersion();
    }

    private void incrementVersionNumber(final long id,
            final int version) throws SQLException {

        final PreparedStatement u = c.prepareStatement(updateVersionSql);
        u.setLong(1, id);
        u.setInt(2, version);
        u.executeUpdate();

    }

    @Override
    public InputStream readContent(final DoxID id) {

        try {
            final PreparedStatement s = c.prepareStatement(readContentSql);
            s.setLong(1, readMeta(id).getId());
            final ResultSet rs = s.executeQuery();
            if (!rs.next()) {
                throw new EntityNotFoundException();
            }
            return rs.getBinaryStream(1);
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public DocumentMeta readMeta(final DoxID id) {

        try {
            final PreparedStatement s = c.prepareStatement(readSql);
            s.setString(1, id.toString());
            final ResultSet rs = s.executeQuery();
            if (!rs.next()) {
                throw new EntityNotFoundException();
            }
            final DocumentMeta meta = new DocumentMeta();
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

    /**
     * This will read the meta data for a record for locking. The version number
     * is not touched.
     *
     * @param id
     * @param version
     * @return
     * @throws SQLException
     */
    private DocumentMeta readMetaAndLock(final DoxID id,
            final int version) throws SQLException {

        final PreparedStatement s = c.prepareStatement(readForUpdateSql);
        s.setString(1, id.toString());
        s.setInt(2, version);
        final ResultSet rs = s.executeQuery();
        if (!rs.next()) {
            throw new OptimisticLockException();
        }
        final DocumentMeta meta = new DocumentMeta();
        meta.setId(rs.getLong(1));
        meta.setDoxId(new DoxID(rs.getString(2)));
        meta.setCreatedBy(new DoxPrincipal(rs.getString(3)));
        meta.setCreatedOn(rs.getTimestamp(4));
        meta.setLastUpdatedBy(new DoxPrincipal(rs.getString(5)));
        meta.setLastUpdatedOn(rs.getTimestamp(6));
        meta.setVersion(rs.getInt(7));
        return meta;
    }

    @Override
    public InputStream readOobContent(final DoxID doxId,
            final String reference) {

        try {
            final PreparedStatement s = c.prepareStatement(oobReadContentSql);
            s.setLong(1, readMeta(doxId).getId());
            s.setString(2, reference);
            final ResultSet rs = s.executeQuery();
            if (!rs.next()) {
                throw new EntityNotFoundException();
            }
            return rs.getBinaryStream(1);
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void updateContent(DoxID doxId,
            InputStream contentStream,
            int version,
            Principal principal) {

        try {
            final Timestamp ts = new Timestamp(System.currentTimeMillis());
            final DocumentMeta meta = readMetaAndLock(doxId, version);
            PreparedStatement u = c.prepareStatement(updateSql);
            u.setBinaryStream(1, contentStream);
            u.setString(2, principal.getName());
            u.setTimestamp(3, ts);
            u.setLong(4, meta.getId());
            u.setInt(5, version);
            u.executeUpdate();
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }
}
