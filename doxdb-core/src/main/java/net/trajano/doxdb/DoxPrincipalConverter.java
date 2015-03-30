package net.trajano.doxdb;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class DoxPrincipalConverter implements AttributeConverter<DoxPrincipal, String> {

    @Override
    public String convertToDatabaseColumn(DoxPrincipal doxPrincipal) {

        return doxPrincipal.toString();
    }

    @Override
    public DoxPrincipal convertToEntityAttribute(String val) {

        return new DoxPrincipal(val);
    }

}
