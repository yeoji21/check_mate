package checkmate.user.application.dto;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckedGoalInfo {

    private final long goalId;
    private final boolean checked;
}
