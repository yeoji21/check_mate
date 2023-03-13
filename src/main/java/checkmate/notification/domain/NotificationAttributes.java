package checkmate.notification.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

@Getter
@EqualsAndHashCode
public class NotificationAttributes {
    private final Map<String, String> attributes;

    public NotificationAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }

    Long getLongValue(String key) {
        return Long.parseLong(String.valueOf(attributes.get(key)));
    }

    String getStringValue(String key) {
        return attributes.get(key);
    }
}
