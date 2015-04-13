package net.trajano.doxdb;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import net.trajano.doxdb.jdbc.DoxPrincipal;

@Converter(autoApply = true)
public class DoxPrincipalConverter implements AttributeConverter<DoxPrincipal, String> {

    @Override
    public String convertToDatabaseColumn(final DoxPrincipal doxPrincipal) {

        return doxPrincipal.toString();
    }

    @Override
    public DoxPrincipal convertToEntityAttribute(final String val) {

        return new DoxPrincipal(val);
    }

}
