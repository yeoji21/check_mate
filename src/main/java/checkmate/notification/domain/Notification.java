package checkmate.notification.domain;

import checkmate.common.domain.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;

@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Notification extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;
    @NotNull
    @Column(name = "user_id")
    private Long userId;
    @NotNull @Enumerated(EnumType.STRING)
    private NotificationType type;
    @NotNull
    private String title;
    @NotNull
    private String body;
    @Getter(value = AccessLevel.PROTECTED)
    @Convert(converter = NotificationAttributeConverter.class)
    private NotificationAttributes attributes = new NotificationAttributes(new HashMap<>());
    @Embedded
    private NotificationReceivers receivers = new NotificationReceivers();

    @Builder
    protected Notification(long userId,
                           NotificationType type,
                           String title,
                           String body,
                           List<NotificationReceiver> receivers) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.body = body;
        setUpReceivers(receivers);
    }

    public void read(long userId) {
        receivers.findReceiver(userId).read();
    }

    public <T> void addAttribute(String key, T value) {
        attributes.addAttribute(key, value.toString());
    }

    public Long getLongAttribute(String key) {
        return attributes.getLongValue(key);
    }

    public String getStringAttribute(String key) {
        return attributes.getStringValue(key);
    }

    public List<NotificationReceiver> getReceivers() {
        return receivers.getReceivers();
    }

    private void setUpReceivers(List<NotificationReceiver> receivers) {
        receivers.forEach(receiver -> {
            this.receivers.addReceiver(receiver);
            receiver.setNotification(this);
        });
    }
}
