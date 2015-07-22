package net.trajano.doxdb.sample;

import java.sql.SQLException;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;

@Stateless
public class Basic {

    /**
     * The data source. It is required that the datasource be XA enabled so it
     * can co-exist with JPA and other operations.
     */
    @Resource
    private DataSource ds;

    public void getds() throws SQLException {

        System.out.println(ds);
        System.out.println(ds.getConnection().getMetaData().getURL());
    }
}
