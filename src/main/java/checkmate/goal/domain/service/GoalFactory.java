package checkmate.goal.domain.service;

import checkmate.goal.application.dto.GoalCommandMapper;
import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;

public class GoalFactory {
    public static Goal createGoal(GoalCreateCommand command, int ongoingGoalCount) {
        Goal goal = GoalCommandMapper.INSTANCE.toGoal(command);
        TeamMate creator = new TeamMate(command.getUserId());
        creator.changeToOngoingStatus(ongoingGoalCount);
        goal.addTeamMate(creator);
        return goal;
    }
}
