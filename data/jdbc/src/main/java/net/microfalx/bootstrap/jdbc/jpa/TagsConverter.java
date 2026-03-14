package net.microfalx.bootstrap.jdbc.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.microfalx.lang.CollectionUtils;

import java.util.Set;

@Converter
public class TagsConverter implements AttributeConverter<Set<String>, String> {

    @Override
    public String convertToDatabaseColumn(Set<String> attribute) {
        return CollectionUtils.setToString(attribute);
    }

    @Override
    public Set<String> convertToEntityAttribute(String attribute) {
        return CollectionUtils.setFromString(attribute);
    }
}
