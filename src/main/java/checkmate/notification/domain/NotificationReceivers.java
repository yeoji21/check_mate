package checkmate.notification.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class NotificationReceivers {
    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<NotificationReceiver> receivers = new ArrayList<>();

    List<NotificationReceiver> getReceivers() {
        return Collections.unmodifiableList(receivers);
    }

    void addReceiver(NotificationReceiver receiver) {
        receivers.add(receiver);
    }

}
