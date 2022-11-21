package checkmate.notification.infrastructure;

import checkmate.notification.application.dto.response.NotificationDetails;
import checkmate.notification.application.dto.response.NotificationDetailsResult;
import checkmate.notification.application.dto.response.QNotificationDetails;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static checkmate.notification.domain.QNotification.notification;
import static checkmate.notification.domain.QNotificationReceiver.notificationReceiver;

@RequiredArgsConstructor
@Repository
public class NotificationQueryDao {
    private final JPAQueryFactory queryFactory;

    public NotificationDetailsResult findNotificationDetailResult(long userId, Long cursorId, Pageable pageable) {
        List<NotificationDetails> notificationDetails = queryFactory
                .select(new QNotificationDetails(notification.id, notification.title, notification.content,
                        notificationReceiver.checked, notification.createdDateTime, notification.type.stringValue()))
                .from(notificationReceiver)
                .join(notificationReceiver.notification, notification)
                .where(notificationReceiver.userId.eq(userId), getNotificationIdLt(cursorId))
                .orderBy(notificationReceiver.notification.id.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();
        boolean hasNext = hasNext(pageable.getPageSize(), notificationDetails);
        return new NotificationDetailsResult(notificationDetails, hasNext);
    }

    private boolean hasNext(int size, List<NotificationDetails> notificationDetails) {
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
