package checkmate.goal.domain;

public class TeamMateCandidate {
    private final TeamMate teamMate;

    public TeamMateCandidate(TeamMate teamMate, int ongoingGoalCount) {
        GoalJoiningPolicy.ongoingGoalCount(ongoingGoalCount);
        this.teamMate = teamMate;
    }

    public void initiate() {
        teamMate.changeToOngoingStatus();
    }
}
