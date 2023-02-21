package checkmate.config.redis;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


// TODO: 2022/10/04 개선 예정
@RequiredArgsConstructor
public class RedisKey {
    public static final String TODAY_GOALS = "todayGoals";
    public static final String ONGOING_GOALS = "ongoingGoals";
    public static final String GOAL_PERIOD = "goalPeriodFind";
    public static final String HISTORY_GOALS = "goalHistory";

    public static List<String> getRedisKeyList(String redisKey, List<Long> userIdList) {
        String date = getToday();
        return userIdList.stream()
                .map(id -> redisKey + "::" + id + "::" + date)
                .collect(Collectors.toList());
    }

    public static List<String> getHistoryKeyList(List<Long> userIdList) {
        return userIdList.stream()
                .map(id -> RedisKey.HISTORY_GOALS + "::" + id)
                .collect(Collectors.toList());
    }

    private static String getToday() {
        LocalDate now = LocalDate.now();
        int year = now.getYear() % 100;
        return year + "." + now.getMonthValue() + "." + now.getDayOfMonth();
    }
}
