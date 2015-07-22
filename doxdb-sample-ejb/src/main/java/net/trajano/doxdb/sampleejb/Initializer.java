package net.trajano.doxdb.sampleejb;

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

        System.out.println("construct" + getClass() + " ds=" + ds);
        System.out.println("construct" + getClass() + " ds=" + ds);

    }
}
