package net.microfalx.bootstrap.jdbc.jpa;

import jakarta.persistence.AttributeConverter;
import net.microfalx.bootstrap.core.utils.EncryptionSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EncryptAttributeConverter implements AttributeConverter<String, String> {

    @Autowired
    private EncryptionSupport encryptionUtil;

    @Override
    public String convertToDatabaseColumn(String s) {
        return encryptionUtil.encrypt(s);
    }

    @Override
    public String convertToEntityAttribute(String s) {
        return encryptionUtil.decrypt(s);
    }

}
