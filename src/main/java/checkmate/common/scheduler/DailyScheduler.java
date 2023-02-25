package checkmate.common.scheduler;

import checkmate.goal.application.GoalCommandService;
import checkmate.mate.application.MateCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@RequiredArgsConstructor
@Component
public class DailyScheduler {
    private final GoalCommandService goalCommandService;
    private final MateCommandService mateCommandService;

    @Scheduled(cron = "0 0 0 1/1 * ?")
    public void updateInitiateGoal() {
        goalCommandService.updateTodayStartGoal();
    }

    @Scheduled(cron = "0 5 0 1/1 * ?")
    public void updateHookyDay() {
        mateCommandService.updateHookyTeamMate();
    }

    @Scheduled(cron = "0 10 0 1/1 * ?")
    public void periodOveredGoalCheck() {
        goalCommandService.updateYesterdayOveredGoals();
    }
}
