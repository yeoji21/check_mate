package checkmate;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCategory;
import checkmate.goal.domain.TeamMate;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationType;
import checkmate.post.domain.Post;
import checkmate.user.domain.UserRole;
import checkmate.user.domain.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

public class TestEntityFactory {

    public static Goal goal(Long id, String title) {
        Goal goal = Goal.builder()
                .category(GoalCategory.ETC)
                .title(title)
                .startDate(LocalDate.now().minusDays(10L))
                .endDate(LocalDate.now().plusDays(30L))
                .checkDays("월화수목금토일")
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
        TeamMate teamMate = new TeamMate(userId);
        ReflectionTestUtils.setField(teamMate, "id", teamMateId);

        teamMate.changeToOngoingStatus(0);
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
                .title("title")
                .body("body")
                .build();
        ReflectionTestUtils.setField(notification, "id", id);
        notification.setNotificationType(type);
        return notification;
    }
}
