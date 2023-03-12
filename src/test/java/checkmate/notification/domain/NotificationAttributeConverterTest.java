package checkmate.notification.domain;

import checkmate.exception.JsonConvertingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

class NotificationAttributeConverterTest {
    @Test
    @DisplayName("NotificationAttributes를 json으로 변환")
    void attributesToJson() throws Exception {
        //given
        NotificationAttributeConverter converter = new NotificationAttributeConverter();
        NotificationAttributes attributes = createNotificationAttributes();

        //when
        String json = converter.convertToDatabaseColumn(attributes);

        //then
        assertThat(json).isEqualTo(getConvertedJsonString());
    }

    @Test
    @DisplayName("json을 NotificationAttributes으로 변환")
    void jsonToAttributes() throws Exception {
        //given
        NotificationAttributeConverter converter = new NotificationAttributeConverter();
        String json = getConvertedJsonString();

        //when
        NotificationAttributes attributes = converter.convertToEntityAttribute(json);

        //then
        assertThat(attributes.getStringValue("abc")).isEqualTo("abcde");
        assertThat(attributes.getStringValue("123")).isEqualTo("a23b2");
        assertThat(attributes.getStringValue("가나다라")).isEqualTo("마바사아");
    }

    @Test
    @DisplayName("json을 NotificationAttributes으로 변환 시 예외 발생")
    void jsonToAttributesException() throws Exception {
        //given
        NotificationAttributeConverter converter = new NotificationAttributeConverter();
        ObjectMapper objectMapper = BDDMockito.mock(ObjectMapper.class);
        ReflectionTestUtils.setField(converter, "objectMapper", objectMapper);

        //when
        throwExceptionWhileConverting(objectMapper.readValue(any(String.class), any(Class.class)));

        //then
        JsonConvertingException exception = assertThrows(JsonConvertingException.class,
                () -> converter.convertToEntityAttribute(getConvertedJsonString()));

        assertThat(exception.getMessage()).isEqualTo(getConvertedJsonString());
    }

    @Test
    @DisplayName("NotificationAttributes를 json으로 변환 시 예외 발생")
    void attributesToJsonException() throws Exception {
        //given
        NotificationAttributeConverter converter = new NotificationAttributeConverter();
        ObjectMapper objectMapper = BDDMockito.mock(ObjectMapper.class);
        ReflectionTestUtils.setField(converter, "objectMapper", objectMapper);

        //when
        throwExceptionWhileConverting(objectMapper.writeValueAsString(any()));

        //then
        JsonConvertingException exception = assertThrows(JsonConvertingException.class,
                () -> converter.convertToDatabaseColumn(createNotificationAttributes()));

        assertThat(exception.getCause().getClass()).isEqualTo(JsonProcessingException.class);
    }

    private void throwExceptionWhileConverting(Object methodCall) throws JsonProcessingException {
        BDDMockito.given(methodCall)
                .willAnswer(invocation -> {
                    Constructor<JsonProcessingException> constructor = JsonProcessingException.class.getDeclaredConstructor(String.class);
                    constructor.setAccessible(true);
                    throw constructor.newInstance("");
                });
    }

    private NotificationAttributes createNotificationAttributes() {
        NotificationAttributes attributes = new NotificationAttributes(new HashMap<>());
        attributes.addAttribute("abc", "abcde");
        attributes.addAttribute("123", "a23b2");
        attributes.addAttribute("가나다라", "마바사아");
        return attributes;
    }

    private String getConvertedJsonString() {
        return "{\"123\":\"a23b2\",\"abc\":\"abcde\",\"가나다라\":\"마바사아\"}";
    }

}