package checkmate.goal.domain.event;

import checkmate.goal.application.TeamMateCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class GoalCreatedEventListener{
    private final TeamMateCommandService teamMateCommandService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void goalCreated(GoalCreatedEvent event) {
        teamMateCommandService.initiatingGoalCreator(event.getGoalId(), event.getUserId());
    }
}
