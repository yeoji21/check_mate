package checkmate.notification.infrastructure;

import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.NotificationType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static checkmate.notification.domain.QNotification.notification;
import static checkmate.notification.domain.QNotificationReceiver.notificationReceiver;


@RequiredArgsConstructor
@Repository
public class NotificationJpaRepository implements NotificationRepository {
    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public void save(Notification notification) {
        entityManager.persist(notification);
    }

    @Override
    public void saveAll(Iterable<Notification> notifications) {
        notifications.forEach(this::save);
    }

    @Override
    public Optional<NotificationReceiver> findReceiver(long notificationId, long receiverUserId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(notificationReceiver)
                        .join(notificationReceiver.notification, notification).fetchJoin()
                        .where(notification.id.eq(notificationId),
                                notificationReceiver.userId.eq(receiverUserId))
                        .fetchOne()
        );
    }

    @Override
    public List<NotificationReceiver> findUncheckedReceivers(long userId, NotificationType notificationType) {
        return queryFactory
                .select(notificationReceiver)
                .from(notificationReceiver)
                .where(notificationReceiver.userId.eq(userId),
                        notificationReceiver.checked.isFalse(),
                        notification.type.eq(notificationType))
                .fetch();
    }
}
