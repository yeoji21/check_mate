package checkmate.goal.application.dto.response;

import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.GoalCategory;
import checkmate.goal.domain.GoalCheckDays;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;


@Getter
public class TodayGoalInfo {
    private long id;
    private GoalCategory category;
    private String title;
    private String checkDays;
    private boolean checked;

    @Builder
    public TodayGoalInfo(long id,
                         GoalCategory category,
                         String title,
                         GoalCheckDays checkDays,
                         LocalDate lastUploadDate) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.checkDays = CheckDaysConverter.toDays(checkDays.intValue());
        this.checked = lastUploadDate != null && lastUploadDate.equals(LocalDate.now());
    }
}
