package net.trajano.doxdb.jdbc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.sql.rowset.serial.SerialBlob;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.DatatypeConverter;

import net.trajano.doxdb.DoxConfiguration;
import net.trajano.doxdb.DoxDAO;
import net.trajano.doxdb.DoxID;

public class JdbcDoxDAO implements DoxDAO {

    private final Connection c;

    private final String copyToTombstoneSql;

    private final String deleteSql;

    private final boolean hasOob;

    private final String insertSql;

    private final long lobSize;

    private final String oobCheckSql;

    final String oobCopyAllToTombstoneSql;

    private final String oobCopyToTombstoneSql;

    final String oobDeleteAllSql;

    private final String oobDeleteSql;

    private final String oobInsertSql;

    /**
     * Size of the lobs for OOB in bytes.
     */
    private final long oobLobSize;

    private final String oobReadAllSql;

    private final String oobReadContentSql;

    private final String oobReadForUpdateSql;

    private final String oobReadSql;

    private final String oobTableName;

    private final String oobTombstoneDeleteSql;

    private final String oobTombstoneTableName;

    private final String oobUpdateSql;

    private final String readContentSql;

    private final String readForUpdateSql;

    private final String readSql;

    private final String tableName;

    private final String tombstoneTableName;

    private final String updateSql;

    private final String updateVersionSql;

    public JdbcDoxDAO(final Connection c, final DoxConfiguration configuration) {

        this.c = c;
        tableName = configuration.getTableName()
                .toUpperCase();
        hasOob = configuration.isHasOob();
        lobSize = configuration.getLobSize();
        oobLobSize = configuration.getOobLobSize();
        tombstoneTableName = (tableName + "TOMBSTONE").toUpperCase();
        oobTableName = (tableName + "OOB").toUpperCase();
        oobTombstoneTableName = (tableName + "OOBTOMBSTONE").toUpperCase();
        try {
            createTable();
            insertSql = String.format("insert into %1$s (CONTENT, DOXID, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, VERSION) values (?, ?,?,?,?,?,?)", tableName);
            oobInsertSql = String.format("insert into %1$s (CONTENT, DOXID, PARENTID, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, VERSION) values (?,?,?,?,?,?,?,?,?)", oobTableName);

            readSql = "select ID, DOXID, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, VERSION from " + tableName + " E where E.DOXID=?";
            oobReadSql = "select ID, DOXID, PARENTID, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, VERSION from " + tableName + "OOB E where E.DOXID=? and E.REFERENCE = ?";
            oobCheckSql = String.format("select id, version from %s where parentid = ? and reference = ? for update", tableName + "OOB");
            oobTombstoneDeleteSql = String.format("delete from %s where doxid = ? and reference = ?", oobTombstoneTableName);
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

            copyToTombstoneSql = String.format("insert into %1$s (CONTENT, DOXID, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, DELETEDBY, DELETEDON) select CONTENT, DOXID, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, ?, ? from %2$s where id = ? and version = ?", tombstoneTableName, tableName);
            oobCopyAllToTombstoneSql = "insert into " + tableName + "OOBTOMBSTONE (CONTENT, DOXID, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, DELETEDBY, DELETEDON) select CONTENT, DOXID, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, ?, ? from " + tableName + "OOB where parentid = ?";
            deleteSql = "delete from " + tableName + " where ID=? and VERSION=?";
            oobReadAllSql = String.format("select CONTENT, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, VERSION from %1$s E where E.PARENTID=?", oobTableName);
            oobDeleteAllSql = "delete from " + tableName + "OOB where PARENTID = ?";
            oobDeleteSql = "delete from " + tableName + "OOB where ID=? and VERSION=?";
            oobCopyToTombstoneSql = String.format("insert into %1$s (CONTENT, DOXID, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, DELETEDBY, DELETEDON) select CONTENT, DOXID, REFERENCE, CREATEDBY, CREATEDON, LASTUPDATEDBY, LASTUPDATEDON, ?, ? from %2$s where parentid = ? and reference = ?", oobTombstoneTableName, oobTableName);

            for (final String sql : new String[] { insertSql, readSql, readContentSql, updateSql, updateVersionSql, deleteSql, readForUpdateSql }) {
                c.prepareStatement(sql)
                        .close();
            }

            if (hasOob) {
                for (final String sql : new String[] { oobReadAllSql, oobInsertSql, oobReadSql, oobReadContentSql, oobUpdateSql, oobDeleteSql, oobReadForUpdateSql, oobCheckSql, oobTombstoneDeleteSql, oobCopyAllToTombstoneSql, oobDeleteAllSql, oobCopyToTombstoneSql }) {
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

        final DocumentMeta meta = readMetaAndLock(doxId, version);

        try (final PreparedStatement check = c.prepareStatement(oobCheckSql)) {
            check.setLong(1, meta.getId());
            check.setString(2, reference);

            try (final ResultSet checkRs = check.executeQuery()) {

                if (checkRs.next()) {
                    // if the reference record already exists then do an
                    // update
                    final long existingId = checkRs.getLong(1);
                    final int existingVersion = checkRs.getInt(2);

                    try (final PreparedStatement s = c.prepareStatement(oobUpdateSql)) {
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
                }
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }

        // Delete any tombstone data if one exists
        try (final PreparedStatement checkTombStone = c.prepareStatement(oobTombstoneDeleteSql)) {
            checkTombStone.setString(1, doxId.toString());
            checkTombStone.setString(2, reference);
            checkTombStone.executeUpdate();
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }

        // Insert a new OOB record
        try (final PreparedStatement s = c.prepareStatement(oobInsertSql, Statement.RETURN_GENERATED_KEYS)) {
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

        final DoxID doxId = DoxID.generate();
        try (final PreparedStatement s = c.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            final Timestamp ts = new Timestamp(System.currentTimeMillis());
            s.setBinaryStream(1, in);
            s.setString(2, doxId.toString());
            s.setString(3, principal.getName());
            s.setTimestamp(4, ts);
            s.setString(5, principal.getName());
            s.setTimestamp(6, ts);
            s.setInt(7, 1);
            s.executeUpdate();
            try (final ResultSet rs = s.getGeneratedKeys()) {
                rs.next();
                return doxId;
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    private void createOobTables() throws SQLException {

        try (final ResultSet tables = c.getMetaData()
                .getTables(null, null, oobTableName, null)) {
            if (tables.next()) {
                throw new PersistenceException("OOB tables for " + tableName + " exist when they are not expected to exist");
            }

            try (PreparedStatement s = c.prepareStatement(String.format("CREATE TABLE %1$s (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, PARENTID BIGINT NOT NULL, CONTENT BLOB(%2$d) NOT NULL, REFERENCE VARCHAR(128) NOT NULL, CREATEDBY VARCHAR(128) NOT NULL, CREATEDON TIMESTAMP NOT NULL, DOXID VARCHAR(32) NOT NULL, LASTUPDATEDBY VARCHAR(128) NOT NULL, LASTUPDATEDON TIMESTAMP NOT NULL, VERSION INTEGER NOT NULL, PRIMARY KEY (ID))", oobTableName, oobLobSize))) {
                s.executeUpdate();
            }
            try (PreparedStatement s = c.prepareStatement(String.format("ALTER TABLE %1$s add unique (DOXID, REFERENCE)", oobTableName))) {
                s.executeUpdate();
            }
            try (PreparedStatement s = c.prepareStatement(String.format("ALTER TABLE %1$s add unique (PARENTID, REFERENCE)", oobTableName))) {
                s.executeUpdate();
            }
            try (PreparedStatement s = c.prepareStatement(String.format("ALTER TABLE %1$s add foreign key (PARENTID, DOXID) references %2$s (ID, DOXID)", oobTableName, tableName))) {
                s.executeUpdate();
            }
            try (PreparedStatement s = c.prepareStatement(String.format("CREATE TABLE %1$s (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONTENT BLOB(%2$d) NOT NULL, REFERENCE VARCHAR(128) NOT NULL, CREATEDBY VARCHAR(128) NOT NULL, CREATEDON TIMESTAMP NOT NULL, DOXID VARCHAR(32) NOT NULL, LASTUPDATEDBY VARCHAR(128) NOT NULL, LASTUPDATEDON TIMESTAMP NOT NULL, DELETEDBY VARCHAR(128) NOT NULL, DELETEDON TIMESTAMP NOT NULL, PRIMARY KEY (ID))", oobTombstoneTableName, oobLobSize))) {
                s.executeUpdate();
            }
            try (PreparedStatement s = c.prepareStatement(String.format("ALTER TABLE %1$s add unique (DOXID, REFERENCE)", oobTombstoneTableName))) {
                s.executeUpdate();
            }

        }
    }

    public void createTable() {

        try (final ResultSet tables = c.getMetaData()
                .getTables(null, null, tableName.toUpperCase(), null)) {
            if (!tables.next()) {

                try (PreparedStatement s = c.prepareStatement(String.format("CREATE TABLE %1$s (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONTENT BLOB(%2$d) NOT NULL, CREATEDBY VARCHAR(128) NOT NULL, CREATEDON TIMESTAMP NOT NULL, DOXID VARCHAR(32) NOT NULL, LASTUPDATEDBY VARCHAR(128) NOT NULL, LASTUPDATEDON TIMESTAMP NOT NULL, VERSION INTEGER, PRIMARY KEY (ID))", tableName, lobSize))) {
                    s.executeUpdate();
                }

                try (PreparedStatement s = c.prepareStatement(String.format("ALTER TABLE %1$s add unique (DOXID)", tableName))) {
                    s.executeUpdate();
                }

                try (PreparedStatement s = c.prepareStatement(String.format("ALTER TABLE %1$s add unique (ID, DOXID)", tableName))) {
                    s.executeUpdate();
                }

                try (PreparedStatement s = c.prepareStatement(String.format("CREATE TABLE %1$sTOMBSTONE (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONTENT BLOB(%2$d) NOT NULL, CREATEDBY VARCHAR(128) NOT NULL, CREATEDON TIMESTAMP NOT NULL, DOXID VARCHAR(32) NOT NULL, LASTUPDATEDBY VARCHAR(128) NOT NULL, LASTUPDATEDON TIMESTAMP NOT NULL, DELETEDBY VARCHAR(128) NOT NULL, DELETEDON TIMESTAMP NOT NULL, PRIMARY KEY (ID))", tableName, lobSize))) {
                    s.executeUpdate();
                }
                try (PreparedStatement s = c.prepareStatement(String.format("ALTER TABLE %1$sTOMBSTONE  add unique (DOXID)", tableName))) {
                    s.executeUpdate();
                }
                // An OOB table would have a reference label for the parent
                // record
                // but it needs to be unique. However in the tombstone it does
                // not
                // need to be unique. Also on the tombstone it does not
                // reference
                // the record by the primary key because the record may not have
                // been deleted on the primary key level but the OOB data has
                // been
                // removed.
                if (hasOob) {
                    createOobTables();
                }
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

        final DocumentMeta meta = readMetaAndLock(id, version);
        final Timestamp ts = new Timestamp(System.currentTimeMillis());

        try {
            if (hasOob) {
                deleteOob(principal, meta, ts);
            }

            // c.prepareStatement("CREATE TABLE " + tableName +
            // "OOB (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, PARENTID
            // BIGINT NOT NULL, CONTENT BLOB(2147483647) NOT NULL, REFERENCE
            // VARCHAR(128) NOT NULL, CREATEDBY VARCHAR(128) NOT NULL, CREATEDON
            // TIMESTAMP NOT NULL, DOXID VARCHAR(32) NOT NULL, LASTUPDATEDBY
            // VARCHAR(128) NOT NULL, LASTUPDATEDON TIMESTAMP NOT NULL, VERSION
            // INTEGER NOT NULL, PRIMARY KEY (ID))")
            try (final PreparedStatement s = c.prepareStatement(copyToTombstoneSql)) {
                s.setString(1, principal.toString());
                s.setTimestamp(2, ts);
                s.setLong(3, meta.getId());
                s.setInt(4, meta.getVersion());
                s.executeUpdate();
            }
            try (final PreparedStatement t = c.prepareStatement(deleteSql)) {
                t.setLong(1, meta.getId());
                t.setInt(2, meta.getVersion());
                final int deletedRows = t.executeUpdate();
                if (deletedRows != 1) {
                    throw new PersistenceException("problem with the delete");
                }
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Removes the OOB records associated with the Dox.
     *
     * @param principal
     * @param meta
     * @param ts
     * @throws SQLException
     */
    private void deleteOob(final Principal principal,
            final DocumentMeta meta,
            final Timestamp ts) throws SQLException {

        try (final PreparedStatement copy = c.prepareStatement(oobCopyAllToTombstoneSql)) {
            copy.setString(1, principal.toString());
            copy.setTimestamp(2, ts);
            copy.setLong(3, meta.getId());
            final int copyCount = copy.executeUpdate();

            final PreparedStatement del = c.prepareStatement(oobDeleteAllSql);
            del.setLong(1, meta.getId());
            final int delCount = del.executeUpdate();
            if (copyCount != delCount) {
                throw new PersistenceException("Mismatch in moving OOB to tombstone");
            }
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

            final int copyCount;
            try (final PreparedStatement copy = c.prepareStatement(oobCopyToTombstoneSql)) {
                copy.setString(1, principal.toString());
                copy.setTimestamp(2, ts);
                copy.setLong(3, meta.getId());
                copy.setString(4, reference);
                copyCount = copy.executeUpdate();
                if (copyCount != 1) {
                    throw new EntityNotFoundException();
                }
            }

            final String oobDeleteSql = "delete from " + tableName + "OOB where PARENTID = ? AND REFERENCE = ?";
            try (final PreparedStatement del = c.prepareStatement(oobDeleteSql)) {
                del.setLong(1, meta.getId());
                del.setString(2, reference);
                final int delCount = del.executeUpdate();
                if (copyCount != delCount) {
                    throw new PersistenceException("Mismatch in moving OOB to tombstone");
                }
                incrementVersionNumber(meta.getId(), version);
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }

    }

    @Override
    public void exportDox(final DoxID doxID,
            final OutputStream os) throws IOException {

        try {
            final MimeMultipart mimeMultipart = new MimeMultipart();
            mimeMultipart.setSubType("mixed");

            final DocumentMeta meta = readMeta(doxID);
            try (final PreparedStatement s = c.prepareStatement(readContentSql)) {
                s.setLong(1, meta.getId());
                try (final ResultSet rs = s.executeQuery()) {
                    if (!rs.next()) {
                        throw new EntityNotFoundException();
                    }
                    final Blob contentBlob = rs.getBlob(1);
                    final MimeBodyPart mimeBodyPart = new MimeBodyPart(contentBlob.getBinaryStream());
                    mimeBodyPart.setHeader("Created-By", meta.getCreatedBy()
                            .getName());
                    mimeBodyPart.setHeader("Created-On", meta.getCreatedOnString());
                    mimeBodyPart.setHeader("Last-Updated-By", meta.getLastUpdatedBy()
                            .getName());
                    mimeBodyPart.setHeader("Last-Updated-On", meta.getLastUpdatedOnString());
                    mimeBodyPart.setHeader("Content-Length", String.valueOf(contentBlob.length()));
                    mimeBodyPart.setFileName(doxID.toString());
                    mimeMultipart.addBodyPart(mimeBodyPart);
                    contentBlob.free();
                }

            }

            if (hasOob) {
                // "select CONTENT, REFERENCE, CREATEDBY, CREATEDON,
                // LASTUPDATEDBY,
                // LASTUPDATEDON, VERSION from %1$s E where E.PARENTID=?"
                try (final PreparedStatement s = c.prepareStatement(oobReadAllSql)) {
                    s.setLong(1, meta.getId());
                    try (final ResultSet rs = s.executeQuery()) {
                        while (rs.next()) {
                            final Calendar createdOnCal = Calendar.getInstance();
                            createdOnCal.setTimeInMillis(rs.getTimestamp(4)
                                    .getTime());

                            final Calendar lastUpdatedOnCal = Calendar.getInstance();
                            lastUpdatedOnCal.setTimeInMillis(rs.getTimestamp(6)
                                    .getTime());

                            final Blob contentBlob = rs.getBlob(1);
                            final MimeBodyPart mimeBodyPart = new MimeBodyPart(contentBlob.getBinaryStream());
                            mimeBodyPart.setHeader("Created-By", rs.getString(3));
                            mimeBodyPart.setHeader("Created-On", DatatypeConverter.printDateTime(createdOnCal));
                            mimeBodyPart.setHeader("Last-Updated-By", rs.getString(5));
                            mimeBodyPart.setHeader("Last-Updated-On", DatatypeConverter.printDateTime(lastUpdatedOnCal));
                            mimeBodyPart.setHeader("Content-Length", String.valueOf(contentBlob.length()));
                            mimeBodyPart.setFileName(rs.getString(2));
                            mimeMultipart.addBodyPart(mimeBodyPart);
                        }
                    }
                }
            }
            mimeMultipart.writeTo(os);
            // final MimeBodyPart mimeBodyPart = new
            // MimeBodyPart(Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
            // .getInput());
            // mimeBodyPart.setFileName("foo");
            // mimeMultipart.addBodyPart(mimeBodyPart);
            // // MimeMessage mimeMessage = new MimeMessage(session);
            // // mimeMessage.addHeader("Content", "value");
            // final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // mimeMultipart.writeTo(baos);
            // baos.close();
            //
            // final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            //
            // final MimeMultipart mimeMultipartr = new MimeMultipart(new
            // ByteArrayDataSource(baos.toByteArray(),
            // MediaType.MULTIPART_FORM_DATA));
            // Assert.assertEquals(2, mimeMultipartr.getCount());
            // mimeMultipartr.getBodyPart(0)
            // .writeTo(baos2);
            // Assert.assertTrue(new
            // String(baos2.toByteArray()).startsWith("<?xml"));
        } catch (final MessagingException | SQLException e) {
            throw new PersistenceException(e);
        } finally {

        }
    }

    @Override
    public int getVersion(final DoxID id) {

        return readMeta(id).getVersion();
    }

    @Override
    public void importDox(final InputStream is) throws IOException {

        try {
            final MimeMultipart mmp = new MimeMultipart(new ByteArrayDataSource(is, MediaType.MULTIPART_FORM_DATA));

            if (mmp.getCount() == 0) {
                throw new PersistenceException("No data was found for import");
            }

            final BodyPart mainBody = mmp.getBodyPart(0);

            if (!hasOob && mmp.getCount() > 1) {
                throw new PersistenceException("OOB data was found but the table " + tableName + " does not support OOB data");
            }

            final long primaryKey;

            try (final PreparedStatement s = c.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mainBody.writeTo(baos);
                baos.close();
                final SerialBlob contentBlob = new SerialBlob(baos.toByteArray());

                s.setBlob(1, contentBlob);
                s.setString(2, mainBody.getFileName());
                s.setString(3, mainBody.getHeader("Created-By")[0]);
                s.setTimestamp(4, new Timestamp(DatatypeConverter.parseDateTime(mainBody.getHeader("Created-On")[0])
                        .getTimeInMillis()));
                s.setString(5, mainBody.getHeader("Last-Updated-By")[0]);
                s.setTimestamp(6, new Timestamp(DatatypeConverter.parseDateTime(mainBody.getHeader("Last-Updated-On")[0])
                        .getTimeInMillis()));
                s.setInt(7, 1);
                s.executeUpdate();
                try (final ResultSet rs = s.getGeneratedKeys()) {
                    rs.next();
                    primaryKey = rs.getLong(1);
                }
            }

            for (int i = 1; i < mmp.getCount(); ++i) {
                final BodyPart oobBody = mmp.getBodyPart(i);

                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                oobBody.writeTo(baos);
                baos.close();

                try (final PreparedStatement os = c.prepareStatement(oobInsertSql, Statement.RETURN_GENERATED_KEYS)) {
                    final SerialBlob contentBlob = new SerialBlob(baos.toByteArray());

                    os.setBlob(1, contentBlob);
                    os.setString(2, mainBody.getFileName());
                    os.setLong(3, primaryKey);
                    os.setString(4, oobBody.getFileName());
                    os.setString(5, oobBody.getHeader("Created-By")[0]);
                    os.setTimestamp(6, new Timestamp(DatatypeConverter.parseDateTime(oobBody.getHeader("Created-On")[0])
                            .getTimeInMillis()));
                    os.setString(7, oobBody.getHeader("Last-Updated-By")[0]);
                    os.setTimestamp(8, new Timestamp(DatatypeConverter.parseDateTime(oobBody.getHeader("Last-Updated-On")[0])
                            .getTimeInMillis()));
                    os.setInt(9, 1);
                    os.executeUpdate();
                    try (final ResultSet rs = os.getGeneratedKeys()) {
                        rs.next();
                    }
                }
            }

        } catch (MessagingException | SQLException | IOException e) {
            throw new PersistenceException(e);
        }

    }

    private void incrementVersionNumber(final long id,
            final int version) throws SQLException {

        try (final PreparedStatement u = c.prepareStatement(updateVersionSql)) {
            u.setLong(1, id);
            u.setInt(2, version);
            u.executeUpdate();
        }

    }

    @Override
    public int readContent(final DoxID id,
            final ByteBuffer buffer) {

        try (final PreparedStatement s = c.prepareStatement(readContentSql)) {
            s.setLong(1, readMeta(id).getId());
            try (final ResultSet rs = s.executeQuery()) {
                if (!rs.next()) {
                    throw new EntityNotFoundException();
                }
                try (final InputStream ret = rs.getBinaryStream(1)) {
                    return ret.read(buffer.array());
                }
            }
        } catch (final IOException | SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void readContentToStream(final DoxID id,
            final OutputStream os) throws IOException {

        try (final PreparedStatement s = c.prepareStatement(readContentSql)) {
            s.setLong(1, readMeta(id).getId());
            try (final ResultSet rs = s.executeQuery()) {
                if (!rs.next()) {
                    throw new EntityNotFoundException();
                }
                try (final InputStream ret = rs.getBinaryStream(1)) {
                    int c = ret.read();
                    while (c != -1) {
                        os.write(c);
                        c = ret.read();
                    }
                }
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public DocumentMeta readMeta(final DoxID id) {

        try (final PreparedStatement s = c.prepareStatement(readSql)) {
            s.setString(1, id.toString());
            try (final ResultSet rs = s.executeQuery()) {
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
            }
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
            final int version) {

        try (final PreparedStatement s = c.prepareStatement(readForUpdateSql)) {
            s.setString(1, id.toString());
            s.setInt(2, version);
            try (final ResultSet rs = s.executeQuery()) {
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
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public int readOobContent(final DoxID doxId,
            final String reference,
            final ByteBuffer buffer) {

        try (final PreparedStatement s = c.prepareStatement(oobReadContentSql)) {

            s.setLong(1, readMeta(doxId).getId());
            s.setString(2, reference);
            final ResultSet rs = s.executeQuery();
            if (!rs.next()) {
                throw new EntityNotFoundException();
            }
            try (final InputStream ret = rs.getBinaryStream(1)) {
                return ret.read(buffer.array());
            }

        } catch (final IOException | SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void readOobContentToStream(final DoxID doxId,
            final String reference,
            final OutputStream os) throws IOException {

        try (final PreparedStatement s = c.prepareStatement(oobReadContentSql)) {

            s.setLong(1, readMeta(doxId).getId());
            s.setString(2, reference);
            final ResultSet rs = s.executeQuery();
            if (!rs.next()) {
                throw new EntityNotFoundException();
            }
            try (final InputStream ret = rs.getBinaryStream(1)) {
                int c = ret.read();
                while (c != -1) {
                    os.write(c);
                    c = ret.read();
                }
            }

        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void updateContent(final DoxID doxId,
            final InputStream contentStream,
            final int version,
            final Principal principal) {

        try {
            final Timestamp ts = new Timestamp(System.currentTimeMillis());
            final DocumentMeta meta = readMetaAndLock(doxId, version);
            final PreparedStatement u = c.prepareStatement(updateSql);
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
