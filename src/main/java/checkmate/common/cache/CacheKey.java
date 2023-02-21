package checkmate.common.cache;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// TODO: 2023/02/22 적용하려면 스프링의 @Cacheable같은 어노테이션과 포맷 맞춰야 함
@RequiredArgsConstructor
public enum CacheKey {
    TODAY_GOALS("today_goals"),
    ONGOING_GOALS("ongoing_goals"),
    GOAL_PERIOD("goal_period"),
    HISTORY_GOALS("history_goals");
    private final String key;

    public static List<String> getRedisKeyList(CacheKey key, List<Long> userIds) {
        String date = getToday();
        return userIds.stream()
                .map(id -> key.key + "::" + id + "::" + date)
                .collect(Collectors.toList());
    }

    private static String getToday() {
        LocalDate now = LocalDate.now();
        int year = now.getYear() % 100;
        return year + "." + now.getMonthValue() + "." + now.getDayOfMonth();
    }
}
