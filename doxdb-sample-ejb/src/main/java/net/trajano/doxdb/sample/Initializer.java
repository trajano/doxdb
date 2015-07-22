package net.trajano.doxdb.sample;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.sql.DataSource;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.jdbc.DoxPrincipal;
import net.trajano.doxdb.search.IndexView;
import net.trajano.doxdb.search.SearchResult;

@Singleton
@Startup
public class Initializer {

    @EJB
    Basic basic;

    @EJB
    SampleBean sampleBean;

    @EJB
    SampleJsonBean sampleJsonBean;

    @EJB
    SampleIndexer indexer;

    @Resource
    DataSource ds;

    @PostConstruct
    public void init() {

        System.out.println("construct");
        byte[] bytes = "helloworld".getBytes();
        DoxID id = sampleBean.create(new ByteArrayInputStream(bytes), new DoxPrincipal("HELLO"));
        System.out.println(id);
        System.out.println(basic);
        System.out.println(indexer);
        System.out.println(sampleJsonBean);

        try {
            basic.getds();
            System.out.println("----");
            sampleBean.readContentToStream(id, System.out);
            IndexView indexView = new IndexView();
            indexView.setIndex("test");
            indexView.setDoxID(new DoxID("abcdefghijabcdefghijabcdefghijab"));
            indexView.appendText("foo bar");
            indexer.addToIndex(indexView);
            System.out.println("----");
            SearchResult result = indexer.search("test", "foo", 5);
            System.out.println(result);
            System.out.println(result.getTotalHits());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
