package checkmate.goal.presentation.dto.response;

import checkmate.goal.application.dto.response.TeamMateInfo;
import checkmate.goal.domain.GoalCategory;
import checkmate.goal.domain.GoalStatus;
import checkmate.goal.domain.Uploadable;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Getter
public class GoalDetailResponse {
    private long id;
    private List<TeamMateInfo> teamMates;
    private GoalCategory category;
    private String title;
    private String goalMethod;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime appointmentTime;
    private String weekDays;
    private GoalStatus goalStatus;
    private boolean inviteable;
    // TODO: 2022/11/03 제거대상
    private Integer minimumLike;
    private Uploadable uploadable;

    @Builder
    public GoalDetailResponse(long id,
                              List<TeamMateInfo> teamMates,
                              GoalCategory category,
                              String title,
                              String goalMethod,
                              LocalDate startDate,
                              LocalDate endDate,
                              LocalTime appointmentTime,
                              String weekDays,
                              GoalStatus goalStatus,
                              boolean inviteable,
                              Integer minimumLike,
                              Uploadable uploadable) {
        this.id = id;
        this.teamMates = teamMates;
        this.category = category;
        this.title = title;
        this.goalMethod = goalMethod;
        this.startDate = startDate;
        this.endDate = endDate;
        this.appointmentTime = appointmentTime;
        this.weekDays = weekDays;
        this.goalStatus = goalStatus;
        this.inviteable = inviteable;
        this.minimumLike = minimumLike;
        this.uploadable = uploadable;
    }
}
