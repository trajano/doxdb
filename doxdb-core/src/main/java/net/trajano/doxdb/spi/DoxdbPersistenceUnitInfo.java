package net.trajano.doxdb.spi;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

public class DoxdbPersistenceUnitInfo implements PersistenceUnitInfo {

    private List<ClassTransformer> transformers = new ArrayList<>();

    @Override
    public void addTransformer(ClassTransformer transformer) {

        transformers.add(transformer);

    }

    @Override
    public boolean excludeUnlistedClasses() {

        return true;
    }

    @Override
    public ClassLoader getClassLoader() {

        return Thread.currentThread()
                .getContextClassLoader();
    }

    @Override
    public List<URL> getJarFileUrls() {

        return Collections.emptyList();
    }

    @Override
    public DataSource getJtaDataSource() {

        return null;
    }

    @Override
    public List<String> getManagedClassNames() {

        return new ArrayList<>();
    }

    @Override
    public List<String> getMappingFileNames() {

        return new ArrayList<>();
    }

    @Override
    public ClassLoader getNewTempClassLoader() {

        return Thread.currentThread()
                .getContextClassLoader();
    }

    @Override
    public DataSource getNonJtaDataSource() {

        return null;
    }

    @Override
    public String getPersistenceProviderClassName() {

        return "";
    }

    @Override
    public String getPersistenceUnitName() {

        return "doxdb";
    }

    @Override
    public URL getPersistenceUnitRootUrl() {

        try {
            return URI.create("http://doxdb.trajano.net/")
                    .toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {

        return null;
    }

    @Override
    public Properties getProperties() {

        final Properties properties = new Properties();


        // .build());

        return properties;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {

        return SharedCacheMode.UNSPECIFIED;
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {

        return null;
    }

    @Override
    public ValidationMode getValidationMode() {

        return ValidationMode.AUTO;
    }

}
