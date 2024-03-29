package checkmate.goal.application.dto.response;

import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.Goal.GoalCategory;
import checkmate.goal.domain.GoalCheckDays;
import com.querydsl.core.annotations.QueryProjection;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;


@Getter
public class TodayGoalInfo implements Serializable {

    private long goalId;
    private GoalCategory category;
    private String title;
    private String checkDays;
    private boolean checked;

    @QueryProjection
    @Builder
    public TodayGoalInfo(
        long goalId,
        GoalCategory category,
        String title,
        GoalCheckDays checkDays,
        LocalDate lastUploadDate) {
        this.goalId = goalId;
        this.category = category;
        this.title = title;
        this.checkDays = CheckDaysConverter.toKorean(checkDays);
        this.checked = lastUploadDate != null && lastUploadDate.equals(LocalDate.now());
    }
}
