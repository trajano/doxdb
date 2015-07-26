package net.trajano.doxdb.jdbc;

/**
 * Configuration for a Dox
 *
 * @author Archimedes
 */
public class DoxConfiguration {

    private boolean hasOob;

    private boolean hasTemporal;

    /**
     * Size of the LOB containing the Dox data. Defaults to 1GB.
     */
    private long lobSize = 1073741824;

    /**
     * Size of the LOB containing the OOB data. Defaults to 2GB.
     */
    private long oobLobSize = 2147483647;

    private String tableName;

    public DoxConfiguration() {

    }

    public DoxConfiguration(final String tableName) {

        this.tableName = tableName;
    }

    public long getLobSize() {

        return lobSize;
    }

    public long getOobLobSize() {

        return oobLobSize;
    }

    public String getTableName() {

        return tableName;
    }

    public boolean isHasOob() {

        return hasOob;
    }

    public boolean isHasTemporal() {

        return hasTemporal;
    }

    public void setHasOob(final boolean hasOob) {

        this.hasOob = hasOob;
    }

    public void setHasTemporal(final boolean hasTemporal) {

        this.hasTemporal = hasTemporal;
    }

    public void setLobSize(long lobSize) {

        this.lobSize = lobSize;
    }

    public void setOobLobSize(long size) {

        oobLobSize = size;
    }

    public void setTableName(final String tableName) {

        this.tableName = tableName;
    }
}
