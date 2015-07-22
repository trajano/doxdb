package net.trajano.doxdb.jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;

import net.trajano.doxdb.DoxFactory;

/**
 * Like an Entity Manager Factory, this is a singleton in relation to an
 * application. It is expected that an EJB jar will load this through the
 * ejb-jar.xml file. The DoxFactory will create the necessary tables.
 *
 * @author Archimedes
 */
public class JdbcDoxFactory implements
    DoxFactory {

    public JdbcDoxFactory() {

    }

    @Override
    public void close() {

    }

    @PostConstruct
    public void init() {

        System.out.println("init from factory");
        try (BufferedReader is = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/dox.xml")))) {
            String line = is.readLine();
            while (line != null) {
                System.out.println(line);
                line = is.readLine();
            }
        } catch (final IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
