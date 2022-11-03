package checkmate.goal.application.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LikeCountCreateCommand {
    private long userId;
    private long goalId;
    private int likeCount;

    @Builder
    public LikeCountCreateCommand(long userId, long goalId, int likeCount) {
        this.userId = userId;
        this.goalId = goalId;
        this.likeCount = likeCount;
    }
}
