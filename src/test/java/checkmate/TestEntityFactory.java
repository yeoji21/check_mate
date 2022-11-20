package checkmate;

import checkmate.goal.domain.*;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationType;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRole;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.Collections;

public class TestEntityFactory {

    public static Goal goal(Long id, String title) {
        Goal goal = Goal.builder()
                .category(GoalCategory.ETC)
                .title(title)
                .startDate(LocalDate.now().minusDays(10L))
                .endDate(LocalDate.now().plusDays(30L))
                .checkDays(new GoalCheckDays("월화수목금토일"))
                .build();
        ReflectionTestUtils.setField(goal, "id", id);
        return goal;
    }

    public static User user(Long id, String name) {
        User user = User.builder()
                .username(name)
                .nickname(name)
                .providerId(name)
                .email(name + "@mail.com")
                .role(UserRole.USER.getRole())
                .fcmToken("fcmToken")
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static TeamMate teamMate(Long teamMateId, long userId) {
        TeamMate teamMate;
        try {
            Constructor<TeamMate> constructor = TeamMate.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            teamMate = constructor.newInstance();
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
            throw new IllegalArgumentException(e);
        }
        ReflectionTestUtils.setField(teamMate, "id", teamMateId);
        ReflectionTestUtils.setField(teamMate, "status", TeamMateStatus.ONGOING);
        return teamMate;
    }

    public static Post post(TeamMate uploader) {
        return Post.builder()
                .teamMate(uploader)
                .text("test post")
                .build();
    }

    public static Notification notification(long id, long userId, NotificationType type) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title("title")
                .body("body")
                .receivers(Collections.EMPTY_LIST)
                .build();
        ReflectionTestUtils.setField(notification, "id", id);
        return notification;
    }
}
