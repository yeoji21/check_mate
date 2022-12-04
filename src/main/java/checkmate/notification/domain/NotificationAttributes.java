package checkmate.notification.domain;

import lombok.EqualsAndHashCode;

import java.util.Map;

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

    Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        StringBuilder attributeStr = new StringBuilder();
        for (String key : attributes.keySet()) {
            attributeStr.append(key).append(" : ").append(attributes.get(key));
        }
        return "NotificationAttributes{" + "attributes=" + attributeStr.toString() + '}';
    }
}
