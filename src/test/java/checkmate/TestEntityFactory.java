package checkmate;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.Goal.GoalCategory;
import checkmate.goal.domain.GoalCheckDays;
import checkmate.goal.domain.GoalPeriod;
import checkmate.mate.domain.Mate;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationType;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import org.springframework.test.util.ReflectionTestUtils;

public class TestEntityFactory {

    public static Goal goal(Long id, String title) {
        Goal goal = Goal.builder()
            .category(GoalCategory.ETC)
            .title(title)
            .period(new GoalPeriod(LocalDate.now(), LocalDate.now().plusDays(30L)))
            .checkDays(GoalCheckDays.ofDayOfWeek(DayOfWeek.values()))
            .build();
        ReflectionTestUtils.setField(goal, "id", id);
        return goal;
    }

    public static User user(Long id, String name) {
        User user = User.builder()
            .username(name)
            .nickname(name)
            .identifier(name)
            .emailAddress(name + "@mail.com")
            .fcmToken("fcmToken")
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static Post post(Mate uploader) {
        return Post.builder()
            .mate(uploader)
            .content("test post")
            .build();
    }

    public static Notification notification(long id, long userId, NotificationType type) {
        Notification notification = Notification.builder()
            .userId(userId)
            .type(type)
            .title("title")
            .content("body")
            .receivers(Collections.EMPTY_LIST)
            .build();
        ReflectionTestUtils.setField(notification, "id", id);
        return notification;
    }
}
