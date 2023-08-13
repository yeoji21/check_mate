package checkmate.common.cache;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CacheKeyUtil {

    public static final String TODAY_GOALS = "today_goals";
    public static final String ONGOING_GOALS = "ongoing_goals";
    public static final String GOAL_PERIOD = "goal_period";
    public static final String HISTORY_GOALS = "history_goals";

    public static String getKeyName(String key, long userId) {
        return key + "::" + userId + "::" + getToday();
    }

    private static String getToday() {
        LocalDate now = LocalDate.now();
        return now.format(DateTimeFormatter.ofPattern("yy.MM.dd"));
    }
}
