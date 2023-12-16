package checkmate.goal.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GoalPolicyConstants {

    public static final int ONGOING_GOAL_COUNT_LIMIT = 10;
    public static final double INVITE_ACCEPTABLE_PROGRESSED_PERCENT_LIMIT = 25.0;
    public static final int GOAL_SKIP_LIMIT_PERCENT = 10;
}
