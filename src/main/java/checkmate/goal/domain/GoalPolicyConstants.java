package checkmate.goal.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GoalPolicyConstants {

    public static final int ONGOING_GOAL_COUNT_LIMIT = 10;
    public static final double MAX_ACCEPTABLE_PERCENT = 25.0;
    public static final int GOAL_SKIP_LIMIT_PERCENT = 10;
}
