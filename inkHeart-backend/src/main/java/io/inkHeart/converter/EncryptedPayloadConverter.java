package io.inkHeart.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.inkHeart.dto.EncryptedPayload;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

@Converter(autoApply = false)
public class EncryptedPayloadConverter implements AttributeConverter<EncryptedPayload, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(EncryptedPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not convert EncryptedPayload to JSON", e);
        }
    }

    @Override
    public EncryptedPayload convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, EncryptedPayload.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not convert JSON to EncryptedPayload type", e);
        }
    }

}
