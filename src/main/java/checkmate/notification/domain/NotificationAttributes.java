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

    public void addAttribute(NotificationAttributeKey key, String value) {
        attributes.put(key.getKey(), value);
    }

    Long getLongValue(NotificationAttributeKey key) {
        return Long.parseLong(String.valueOf(attributes.get(key.getKey())));
    }

    String getStringValue(NotificationAttributeKey key) {
        return attributes.get(key.getKey());
    }
}
