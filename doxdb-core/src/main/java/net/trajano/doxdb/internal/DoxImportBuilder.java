package net.trajano.doxdb.internal;

import java.io.InputStream;
import java.security.Principal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.jdbc.DoxPrincipal;

/**
 * Builder used to create the Data and meta-data required for importing a single
 * Dox with all their OOB data. This uses a fluent API.
 *
 * @author Archimedes
 */
public class DoxImportBuilder {

    public static class DoxOobImportDetails {

        private InputStream contentStream;

        private Principal createdBy;

        private Date createdOn;

        private Principal lastUpdatedBy;

        private Date lastUpdatedOn;

        private String reference;

        public InputStream getContentStream() {

            return getContentStream();
        }

        public Principal getCreatedBy() {

            return createdBy;
        }

        public Date getCreatedOn() {

            return createdOn;
        }

        public Principal getLastUpdatedBy() {

            return lastUpdatedBy;
        }

        public Date getLastUpdatedOn() {

            return lastUpdatedOn;
        }

        public String getReference() {

            return reference;
        }

        public boolean isComplete() {

            return reference != null && contentStream != null && createdBy != null && createdOn != null && lastUpdatedBy != null && lastUpdatedOn != null;
        }

    }

    private InputStream contentStream;

    private Principal createdBy;

    private Date createdOn;

    private DoxOobImportDetails currentOobImportDetails = null;

    private DoxID id;

    private Principal lastUpdatedBy;

    private Date lastUpdatedOn;

    /**
     * OOB import details list.
     */
    private final List<DoxOobImportDetails> oobImportDetailsList = new LinkedList<>();

    public DoxImportBuilder contentStream(InputStream contentStream) {

        this.contentStream = contentStream;
        return this;
    }

    public DoxImportBuilder createdBy(Principal by) {

        createdBy = by;
        return this;
    }

    public DoxImportBuilder createdBy(String by) {

        return createdBy(new DoxPrincipal(by));
    }

    public DoxImportBuilder createdOn(Date on) {

        createdOn = on;
        return this;
    }

    public DoxImportBuilder createdOn(String on) {

        return createdOn(DatatypeConverter.parseDateTime(on)
                .getTime());
    }

    public DoxImportBuilder doxID(DoxID id) {

        this.id = id;
        return this;
    }

    /**
     * Convenience method that uses a string.
     *
     * @param doxID
     * @return
     */
    public DoxImportBuilder doxID(String doxID) {

        return doxID(new DoxID(doxID));
    }

    public InputStream getContentStream() {

        return contentStream;
    }

    public Principal getCreatedBy() {

        return createdBy;
    }

    public Date getCreatedOn() {

        return createdOn;
    }

    public DoxID getDoxID() {

        return id;
    }

    public Principal getLastUpdatedBy() {

        return lastUpdatedBy;
    }

    public Date getLastUpdatedOn() {

        return lastUpdatedOn;
    }

    public Iterable<DoxOobImportDetails> getOobImportDetails() {

        return oobImportDetailsList;
    }

    public boolean hasOob() {

        return !oobImportDetailsList.isEmpty();
    }

    /**
     * Ensures that all the data is complete.
     *
     * @return
     */
    public boolean isComplete() {

        for (final DoxOobImportDetails oobImportDetails : oobImportDetailsList) {
            if (!oobImportDetails.isComplete()) {
                return false;
            }
        }
        return id != null && contentStream != null && createdBy != null && createdOn != null && lastUpdatedBy != null && lastUpdatedOn != null;
    }

    public DoxImportBuilder lastUpdatedBy(Principal by) {

        lastUpdatedBy = by;
        return this;
    }

    public DoxImportBuilder lastUpdatedBy(String by) {

        return lastUpdatedBy(new DoxPrincipal(by));
    }

    public DoxImportBuilder lastUpdatedOn(Date on) {

        lastUpdatedOn = on;
        return this;
    }

    public DoxImportBuilder lastUpdatedOn(String on) {

        return lastUpdatedOn(DatatypeConverter.parseDateTime(on)
                .getTime());
    }

    /**
     * Starts a new blank OOB import details and adds it to the import list.
     * This needs to be done before doing any of the other oob methods.
     *
     * @return
     */
    public DoxImportBuilder newOobImportDetails() {

        currentOobImportDetails = new DoxOobImportDetails();
        oobImportDetailsList.add(currentOobImportDetails);
        return this;
    }

    public DoxImportBuilder oobContentStream(InputStream contentStream) {

        currentOobImportDetails.contentStream = contentStream;
        return this;
    }

    public DoxImportBuilder oobCreatedBy(Principal by) {

        currentOobImportDetails.createdBy = by;
        return this;
    }

    public DoxImportBuilder oobCreatedOn(Date on) {

        currentOobImportDetails.createdOn = on;
        return this;
    }

    public DoxImportBuilder oobLastUpdatedBy(Principal by) {

        currentOobImportDetails.lastUpdatedBy = by;
        return this;
    }

    public DoxImportBuilder oobLastUpdatedOn(Date on) {

        currentOobImportDetails.lastUpdatedOn = on;
        return this;
    }

    public DoxImportBuilder oobReference(String reference) {

        currentOobImportDetails.reference = reference;
        return this;
    }

}
