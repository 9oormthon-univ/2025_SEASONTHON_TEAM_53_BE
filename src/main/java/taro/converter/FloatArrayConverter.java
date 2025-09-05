package taro.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * float[] 배열을 데이터베이스에 저장하기 위한 컨버터
 * H2: JSON 문자열로 저장
 * PostgreSQL: float[] 타입으로 저장 (native 지원)
 */
@Slf4j
@Converter
public class FloatArrayConverter implements AttributeConverter<float[], String> {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convertToDatabaseColumn(float[] attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error converting float array to JSON", e);
            throw new RuntimeException("Error converting float array to JSON", e);
        }
    }
    
    @Override
    public float[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, float[].class);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to float array", e);
            throw new RuntimeException("Error converting JSON to float array", e);
        }
    }
}
