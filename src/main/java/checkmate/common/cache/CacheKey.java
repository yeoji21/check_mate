package checkmate.common.cache;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@UtilityClass
public class CacheKey {
    public static final String TODAY_GOALS = "today_goals";
    public static final String ONGOING_GOALS = "ongoing_goals";
    public static final String GOAL_PERIOD = "goal_period";
    public static final String HISTORY_GOALS = "history_goals";

    public static List<String> ongoingGoalsKeys(List<Long> userIds) {
        return getRedisKeyList(ONGOING_GOALS, userIds);
    }

    public static List<String> todayGoalsKeys(List<Long> userIds) {
        return getRedisKeyList(TODAY_GOALS, userIds);
    }

    public static List<String> historyGoalsKeys(List<Long> userIds) {
        return getRedisKeyList(HISTORY_GOALS, userIds);
    }

    private static List<String> getRedisKeyList(String key, List<Long> userIds) {
        return userIds.stream()
                .map(id -> key + "::" + id + "::" + getToday())
                .collect(Collectors.toList());
    }

    private static String getToday() {
        LocalDate now = LocalDate.now();
        return now.format(DateTimeFormatter.ofPattern("yy.MM.dd"));
    }
}
