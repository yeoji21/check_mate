package checkmate.notification.infrastructure;

import static checkmate.notification.domain.QNotification.notification;
import static checkmate.notification.domain.QNotificationReceiver.notificationReceiver;
import static checkmate.user.domain.QUser.user;

import checkmate.notification.application.dto.response.NotificationDetailInfo;
import checkmate.notification.application.dto.response.NotificationDetailResult;
import checkmate.notification.application.dto.response.QNotificationDetailInfo;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class NotificationQueryDao {

    private final JPAQueryFactory queryFactory;

    public NotificationDetailResult findNotificationDetailResult(long userId, Long cursorId,
        Pageable pageable) {
        List<NotificationDetailInfo> notifications = queryFactory
            .select(new QNotificationDetailInfo(notification.id, notification.title,
                notification.content,
                notificationReceiver.isRead, notification.createdDateTime,
                notification.type.stringValue()))
            .from(notificationReceiver)
            .join(notificationReceiver.notification, notification)
            .where(notificationReceiver.userId.eq(userId), getNotificationIdLt(cursorId))
            .orderBy(notificationReceiver.notification.id.desc())
            .limit(pageable.getPageSize() + 1)
            .fetch();
        boolean hasNext = hasNext(pageable.getPageSize(), notifications);
        return new NotificationDetailResult(notifications, hasNext);
    }

    public List<String> findReceiversFcmToken(long notificationId) {
        return queryFactory.select(user.fcmToken)
            .from(notificationReceiver)
            .innerJoin(user).on(notificationReceiver.userId.eq(user.id))
            .where(notificationReceiver.notification.id.eq(notificationId))
            .fetch();
    }

    private boolean hasNext(int size, List<NotificationDetailInfo> notificationDetails) {
        boolean hasNext = false;
        if (notificationDetails.size() > size) {
            notificationDetails.remove(size);
            hasNext = true;
        }
        return hasNext;
    }

    private BooleanExpression getNotificationIdLt(Long cursorId) {
        return cursorId != null ? notification.id.lt(cursorId) : null;
    }
}
