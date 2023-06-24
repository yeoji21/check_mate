package checkmate.goal.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


public class GoalScheduleService {

    public static String createGoalSchedule(GoalPeriod period, GoalCheckDays checkDays) {
        return convertToSchedule(period, date -> checkDays.isCheckDay(date) ? "1" : "0");
    }

    public static String createCheckedSchedule(GoalPeriod period, GoalCheckDays checkDays,
        List<LocalDate> checkedDates) {
        return convertToSchedule(period,
            date -> checkDays.isCheckDay(date) && isUploaded(checkedDates, date) ? "1" : "0");
    }

    private static String convertToSchedule(
        GoalPeriod period,
        Function<LocalDate, String> mappingFunction) {
        return period.getFullPeriodStream()
            .map(mappingFunction)
            .collect(Collectors.joining());
    }

    private static boolean isUploaded(List<LocalDate> uploadedDates, LocalDate date) {
        return uploadedDates.contains(date);
    }

}
