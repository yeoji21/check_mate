package checkmate.goal.application.dto.request;

import lombok.Builder;

@Builder
public record LikeCountCreateCommand(
        long userId,
        long goalId,
        int likeCount) {
}
