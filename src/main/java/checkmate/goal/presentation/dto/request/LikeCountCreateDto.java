package checkmate.goal.presentation.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeCountCreateDto {
    @NotNull
    private Long goalId;
    @NotNull
    private Integer likeCount;

    public LikeCountCreateDto(long goalId, int likeCount) {
        this.goalId = goalId;
        this.likeCount = likeCount;
    }
}
