package checkmate.notification.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Map;


// TODO: 2022/12/04 리팩토링
@Component
@Converter
public class NotificationAttributeConverter implements AttributeConverter<NotificationAttributes, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public String convertToDatabaseColumn(NotificationAttributes attribute) {
        try {
            return objectMapper.writeValueAsString(attribute.getAttributes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public NotificationAttributes convertToEntityAttribute(String dbData) {
        try {
            return new NotificationAttributes(objectMapper.readValue(dbData, Map.class));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @SneakyThrows
    public String attributesToJson(Notification notification){
        return convertToDatabaseColumn(notification.getAttributes());
    }
}
