package checkmate.goal.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


public class GoalScheduleService {

    public static String createGoalSchedule(Goal goal) {
        return convertToSchedule(getPeriod(goal), date -> isCheckDay(goal, date) ? "1" : "0");
    }

    public static String createCheckedSchedule(Goal goal, List<LocalDate> checkedDates) {
        return convertToSchedule(getPeriod(goal),
            date -> isCheckDay(goal, date) && isUploaded(checkedDates, date) ? "1" : "0");
    }

    private static String convertToSchedule(GoalPeriod period,
        Function<LocalDate, String> mappingFunction) {
        return period.getFullPeriodStream()
            .map(mappingFunction)
            .collect(Collectors.joining());
    }

    private static GoalPeriod getPeriod(Goal goal) {
        return goal.getPeriod();
    }

    private static boolean isCheckDay(Goal goal, LocalDate date) {
        return goal.getCheckDays().isCheckDay(date);
    }

    private static boolean isUploaded(List<LocalDate> uploadedDates, LocalDate date) {
        return uploadedDates.contains(date);
    }

}
