package checkmate.goal.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public enum GoalStatus {
    ONGOING, OVER, WAITING;
}
