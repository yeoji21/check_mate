package checkmate.notification.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@Entity
public class NotificationReceiver {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_receiver_id")
    private Long id;
    @NotNull
    @Column(name = "user_id")
    private Long userId;
    @NotNull
    private boolean checked;
    @NotNull @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id")
    private Notification notification;

    public void read() {
        this.checked = true;
    }

    public NotificationReceiver(long userId) {
        this.userId = userId;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }
}
