package checkmate.common.scheduler;

import checkmate.goal.application.GoalBatchService;
import checkmate.mate.application.MateBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@RequiredArgsConstructor
@Component
public class DailyScheduler {

    private final GoalBatchService goalBatchService;
    private final MateBatchService mateBatchService;

    @Scheduled(cron = "0 0 0 1/1 * ?")
    public void updateInitiateGoal() {
        goalBatchService.updateTodayStartGoals();
    }

    @Scheduled(cron = "0 5 0 1/1 * ?")
    public void updateHookyDay() {
        mateBatchService.updateUploadSkippedMates();
    }

    @Scheduled(cron = "0 10 0 1/1 * ?")
    public void periodOveredGoalCheck() {
        goalBatchService.updateYesterdayOveredGoals();
    }
}
