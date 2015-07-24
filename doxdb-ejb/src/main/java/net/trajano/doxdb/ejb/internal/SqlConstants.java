package net.trajano.doxdb.ejb.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads up the SQL from the properties file.
 *
 * @author trajanar
 */
public class SqlConstants {

    public static final String COPYTOTOMBSTONESQL;

    public static final String DELETE;

    public static final String INSERT;

    public static final String OOBCHECK;

    public static final String OOBCOPYALLTOTOMBSTONE;

    public static final String OOBCOPYTOTOMBSTONE;

    public static final String OOBDELETE;

    public static final String OOBDELETEALL;

    public static final String OOBINSERT;

    public static final String OOBREAD;

    public static final String OOBREADALL;

    public static final String OOBREADCONTENT;

    public static final String OOBREADFORUPDATE;

    public static final String OOBTOMBSTONEDELETE;

    public static final String OOBUPDATESQL;

    public static final String READ;

    public static final String READALLCONTENT;

    public static final String READCONTENT;

    public static final String READFORUPDATE;

    public static final String UPDATE;

    public static final String UPDATEVERSION;

    static {
        final Properties sqls = new Properties();
        try (InputStream is = SqlConstants.class.getResourceAsStream("/META-INF/sqls.properties")) {
            sqls.load(is);

            INSERT = sqls.getProperty("insert");
            READ = sqls.getProperty("read");
            READFORUPDATE = sqls.getProperty("readForUpdate");
            READCONTENT = sqls.getProperty("readContent");
            READALLCONTENT = sqls.getProperty("readAllContent");
            UPDATE = sqls.getProperty("update");
            UPDATEVERSION = sqls.getProperty("updateVersion");
            DELETE = sqls.getProperty("delete");
            COPYTOTOMBSTONESQL = sqls.getProperty("copyToTombstoneSql");
        } catch (final IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        try (InputStream is = SqlConstants.class.getResourceAsStream("/META-INF/oob-sqls.properties")) {
            sqls.load(is);

            OOBINSERT = sqls.getProperty("oobInsert");
            OOBREAD = sqls.getProperty("oobRead");
            OOBCHECK = sqls.getProperty("oobCheck");
            OOBTOMBSTONEDELETE = sqls.getProperty("oobTombstoneDelete");
            OOBREADFORUPDATE = sqls.getProperty("oobReadForUpdate");
            OOBREADCONTENT = sqls.getProperty("oobReadContent");
            OOBUPDATESQL = sqls.getProperty("oobUpdateSql");
            OOBCOPYALLTOTOMBSTONE = sqls.getProperty("oobCopyAllToTombstone");
            OOBREADALL = sqls.getProperty("oobReadAll");
            OOBDELETEALL = sqls.getProperty("oobDeleteAll");
            OOBDELETE = sqls.getProperty("oobDelete");
            OOBCOPYTOTOMBSTONE = sqls.getProperty("oobCopyToTombstone");
        } catch (final IOException e) {
            throw new ExceptionInInitializerError(e);
        }

    }

    private SqlConstants() {
    }
}
