package net.trajano.doxdb.sample;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.jdbc.DoxPrincipal;

@Singleton
@Startup
public class Initializer {

    @EJB
    Basic basic;

    @EJB
    SampleBean sampleBean;

    @EJB
    SampleJsonBean sampleJsonBean;

    @PostConstruct
    public void init() {

        System.out.println("construct");
        byte[] bytes = "helloworld".getBytes();
        DoxID id = sampleBean.create(new ByteArrayInputStream(bytes), new DoxPrincipal("HELLO"));
        System.out.println(id);
        System.out.println(basic);
        System.out.println(sampleJsonBean);
        basic.getds();

        try {
            System.out.println("----");
            sampleBean.readContentToStream(id, System.out);
            System.out.println("----");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
