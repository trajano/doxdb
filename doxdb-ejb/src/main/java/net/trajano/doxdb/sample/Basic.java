package net.trajano.doxdb.sample;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;

@Stateless
public class Basic {

    /**
     * The data source. It is required that the datasource be XA enabled so it
     * can co-exist with JPA and other operations.
     */
    @Resource(name = "doxdbDataSource2", lookup = "java:comp/DefaultDataSource")
    private DataSource ds;

    public void getds() {

        System.out.println(ds);
    }
}
