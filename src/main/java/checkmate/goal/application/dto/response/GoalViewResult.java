package checkmate.goal.application.dto.response;

public record GoalViewResult(
        GoalDetailInfo goalDetailInfo,
        TeamMateScheduleInfo calenderInfo,
        double progress) {
}
