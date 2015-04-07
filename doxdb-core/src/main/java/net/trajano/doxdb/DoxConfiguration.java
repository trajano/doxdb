package net.trajano.doxdb;

/**
 * Configuration for a Dox
 * 
 * @author Archimedes
 */
public class DoxConfiguration {

    private String tableName;

    private boolean hasOob;

    private boolean hasTemporal;

    public DoxConfiguration() {

    }

    public DoxConfiguration(String tableName) {

        this.tableName = tableName;
    }

    public String getTableName() {

        return tableName;
    }

    public void setTableName(String tableName) {

        this.tableName = tableName;
    }

    public boolean isHasOob() {

        return hasOob;
    }

    public void setHasOob(boolean hasOob) {

        this.hasOob = hasOob;
    }

    public boolean isHasTemporal() {

        return hasTemporal;
    }

    public void setHasTemporal(boolean hasTemporal) {

        this.hasTemporal = hasTemporal;
    }
}
