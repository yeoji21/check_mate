package checkmate.goal.presentation.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeCountCreateDto {
    @NotNull
    private Integer likeCount;

    public LikeCountCreateDto(int likeCount) {
        this.likeCount = likeCount;
    }
}
