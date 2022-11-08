package checkmate.goal.application.dto.response;

public record GoalViewResult(
        GoalDetailInfo goalDetailInfo,
        TeamMateCalendarInfo calenderInfo,
        double progress) {
}
