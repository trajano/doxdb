package net.trajano.doxdb.sample;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.sql.DataSource;

@Singleton
@Startup
public class Initializer {

    @Resource
    DataSource ds;

    @PostConstruct
    public void init() {

        System.out.println("construct=" + ds);
    }
}
