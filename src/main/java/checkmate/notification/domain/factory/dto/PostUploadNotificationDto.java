package checkmate.notification.domain.factory.dto;

import checkmate.goal.domain.Goal;
import checkmate.user.domain.User;
import lombok.Getter;

import java.util.List;

/*
- uploaderUserId, uploaderNickname
- goalTitle, goalId
 */
@Getter
public class PostUploadNotificationDto {
    private long uploaderUserId;
    private String uploderNickname;
    private long goalId;
    private String goalTitle;
    private List<Long> teamMateUserIds;

    public PostUploadNotificationDto(User user, Goal goal, List<Long> teamMateUserIds) {
        this.uploaderUserId = user.getId();
        this.uploderNickname = user.getNickname();
        this.goalId = goal.getId();
        this.goalTitle = goal.getTitle();
        this.teamMateUserIds = teamMateUserIds;
    }
}
