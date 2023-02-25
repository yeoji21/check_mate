package checkmate.goal.presentation.dto;

import lombok.AccessLevel;
import lombok.Builder;
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

    @Builder
    public LikeCountCreateDto(long goalId, int likeCount) {
        this.goalId = goalId;
        this.likeCount = likeCount;
    }
}
