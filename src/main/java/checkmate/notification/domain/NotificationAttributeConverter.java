package checkmate.notification.domain;

import checkmate.exception.JsonConvertingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Map;


@Component
@Converter
public class NotificationAttributeConverter implements AttributeConverter<NotificationAttributes, String> {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public String convertToDatabaseColumn(NotificationAttributes attribute) {
        try {
            return objectMapper.writeValueAsString(attribute.getAttributes());
        } catch (JsonProcessingException e) {
            throw new JsonConvertingException(e, attribute.toString());
        }
    }

    @Override
    public NotificationAttributes convertToEntityAttribute(String dbData) {
        try {
            return new NotificationAttributes(objectMapper.readValue(dbData, Map.class));
        } catch (JsonProcessingException e) {
            throw new JsonConvertingException(e, dbData);
        }
    }
}
