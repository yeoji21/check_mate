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
import static checkmate.user.domain.QUser.user;


@RequiredArgsConstructor
@Repository
public class NotificationRepositoryImpl implements NotificationRepository {
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
    public Optional<NotificationReceiver> findNotificationReceiver(long notificationId, long userId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(notificationReceiver)
                        .join(notificationReceiver.notification, notification).fetchJoin()
                        .where(notification.id.eq(notificationId),
                                notificationReceiver.userId.eq(userId))
                        .fetchOne()
        );
    }

    @Override
    public List<String> findReceiversFcmToken(Long notificationId) {
        return queryFactory.select(user.fcmToken)
                .from(notificationReceiver)
                .innerJoin(user).on(notificationReceiver.userId.eq(user.id))
                .where(notificationReceiver.notification.id.eq(notificationId))
                .fetch();
    }

    @Override
    public List<NotificationReceiver> findGoalCompleteNotificationReceivers(long userId) {
        return queryFactory
                .select(notificationReceiver)
                .from(notificationReceiver)
                .where(notificationReceiver.userId.eq(userId),
                        notification.type.eq(NotificationType.COMPLETE_GOAL))
                .fetch();
    }

    @Override
    public Optional<Notification> findById(long notificationId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(notification).distinct()
                        .join(notification.receivers.receivers, notificationReceiver).fetchJoin()
                        .where(notification.id.eq(notificationId))
                        .fetchOne());
    }
}
