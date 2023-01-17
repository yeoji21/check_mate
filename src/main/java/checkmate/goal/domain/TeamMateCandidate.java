package checkmate.goal.domain;

// TODO: 2023/01/17 ?
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
