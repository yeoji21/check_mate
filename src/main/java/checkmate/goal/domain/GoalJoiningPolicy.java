package checkmate.goal.domain;

import checkmate.exception.ExceedGoalLimitException;

public class GoalJoiningPolicy {
    private static final int MAX_ONGOING_COUNT = 10;
    private static final double MAX_ACCEPTABLE_PERCENT = 25.0;

    public static void ongoingGoalCount(int count) {
        if(count >= MAX_ONGOING_COUNT) throw new ExceedGoalLimitException();
    }

    public static boolean progressedPercent(double rate) {
        return rate <= MAX_ACCEPTABLE_PERCENT;
    }
}
