package net.trajano.doxdb;

/**
 * Configuration for a Dox
 *
 * @author Archimedes
 */
public class DoxConfiguration {

    private boolean hasOob;

    private boolean hasTemporal;

    private String tableName;

    public DoxConfiguration() {

    }

    public DoxConfiguration(final String tableName) {

        this.tableName = tableName;
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

    public void setTableName(final String tableName) {

        this.tableName = tableName;
    }
}
