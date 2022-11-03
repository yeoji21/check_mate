package checkmate.goal.application.dto;

import checkmate.goal.application.dto.response.GoalDetailInfo;
import checkmate.goal.application.dto.response.GoalDetailViewResult;
import checkmate.goal.application.dto.response.GoalSimpleInfo;
import checkmate.goal.application.dto.response.TeamMateCalendarInfo;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.WeekDays;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GoalQueryMapper {
    GoalQueryMapper INSTANCE = Mappers.getMapper(GoalQueryMapper.class);

    GoalSimpleInfo toGoalSimpleInfo(Goal goal);

    default String toKorWeekDays(WeekDays weekDays){
        return weekDays.getKorWeekDay();
    }

    GoalDetailViewResult toGoalDetailViewResult(GoalDetailInfo goalDetail, TeamMateCalendarInfo teamMateCalendarInfo, double progress);
}
