package net.trajano.doxdb.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import net.trajano.doxdb.DoxID;

@Converter(autoApply = true)
public class DoxIDConverter implements AttributeConverter<DoxID, String> {

    @Override
    public String convertToDatabaseColumn(final DoxID doxid) {

        return doxid.toString();
    }

    @Override
    public DoxID convertToEntityAttribute(final String val) {

        return new DoxID(val);
    }

}
