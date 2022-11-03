package checkmate.goal.application.dto.response;


import checkmate.goal.domain.GoalCategory;
import lombok.*;

import java.io.Serializable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoalSimpleInfo implements Serializable {
    private long id;
    private GoalCategory category;
    private String title;
    private String weekDays;

    @Builder
    public GoalSimpleInfo(long id,
                          GoalCategory category,
                          String title,
                          String weekDays) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.weekDays = weekDays;
    }
}
