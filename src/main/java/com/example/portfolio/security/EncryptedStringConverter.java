package com.example.portfolio.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter that automatically encrypts/decrypts String fields
 * when saving to/loading from the database.
 *
 * Apply to entity fields with: @Convert(converter =
 * EncryptedStringConverter.class)
 */
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static EncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(EncryptionService service) {
        EncryptedStringConverter.encryptionService = service;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (encryptionService == null) {
            throw new IllegalStateException("EncryptionService not initialized");
        }
        return encryptionService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (encryptionService == null) {
            throw new IllegalStateException("EncryptionService not initialized");
        }
        return encryptionService.decrypt(dbData);
    }
}
