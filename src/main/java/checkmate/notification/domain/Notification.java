package checkmate.notification.domain;

import checkmate.common.domain.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Notification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @NotNull
    @Column(name = "user_id")
    private Long userId;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private NotificationType type;
    @NotNull
    @Column(name = "title")
    private String title;
    @NotNull
    @Column(name = "content")
    private String content;
    @Convert(converter = NotificationAttributeConverter.class)
    private NotificationAttributes attributes = new NotificationAttributes(new HashMap<>());
    @Embedded
    private NotificationReceivers receivers = new NotificationReceivers();

    @Builder
    protected Notification(long userId,
                           NotificationType type,
                           String title,
                           String content,
                           List<NotificationReceiver> receivers) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.content = content;
        setUpReceivers(receivers);
    }

    public <T> void addAttribute(NotificationAttributeKey key, T value) {
        attributes.addAttribute(key, value.toString());
    }

    public Long getLongAttribute(NotificationAttributeKey key) {
        return attributes.getLongValue(key);
    }

    public String getStringAttribute(NotificationAttributeKey key) {
        return attributes.getStringValue(key);
    }

    public List<NotificationReceiver> getReceivers() {
        return receivers.getReceivers();
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes.getAttributes());
    }

    private void setUpReceivers(List<NotificationReceiver> receivers) {
        receivers.forEach(receiver -> {
            this.receivers.addReceiver(receiver);
            receiver.setNotification(this);
        });
    }
}
