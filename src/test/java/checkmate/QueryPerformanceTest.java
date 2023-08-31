package checkmate;

import static checkmate.notification.domain.QNotification.notification;
import static checkmate.notification.domain.QNotificationReceiver.notificationReceiver;

import checkmate.config.WebSecurityConfig;
import checkmate.notification.domain.NotificationType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.util.StopWatch;


@Disabled
@Import(WebSecurityConfig.class)
@SpringBootTest
@Transactional
public class QueryPerformanceTest {

    @Autowired
    private JPAQueryFactory queryFactory;
    private StopWatch stopWatch;

    @BeforeEach
    void setUp() {
        stopWatch = new StopWatch();
    }

    @Test
    void findGoalCompleteNotification() throws Exception {
        stopWatch.start();
        queryFactory
            .selectFrom(notification)
            .join(notification.receivers.receivers, notificationReceiver)
            .on(notificationReceiver.isRead.eq(false))
            .where(notificationReceiver.userId.eq(11L),
                notification.type.eq(NotificationType.COMPLETE_GOAL))
            .fetch();
        stopWatch.stop();
        System.out.println("# First : " + stopWatch.getTotalTimeMillis());

        stopWatch.start();
        queryFactory
            .selectFrom(notification).distinct()
            .join(notification.receivers.receivers, notificationReceiver)
            .on(notificationReceiver.isRead.eq(false))
            .where(notificationReceiver.userId.eq(11L),
                notification.type.eq(NotificationType.COMPLETE_GOAL))
            .fetch();
        stopWatch.stop();
        System.out.println("# Second : " + stopWatch.getTotalTimeMillis());
    }
}
