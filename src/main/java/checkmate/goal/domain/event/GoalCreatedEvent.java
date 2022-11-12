package checkmate.goal.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoalCreatedEvent{
    private final long goalId;
    private final long userId;
}
