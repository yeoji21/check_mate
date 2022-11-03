package checkmate.goal.application.dto;

import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.application.dto.response.GoalCreateResult;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalModifyRequest;
import checkmate.goal.domain.TeamMate;
import checkmate.notification.domain.factory.dto.GoalCompleteNotificationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoalCommandMapper {
    GoalCommandMapper INSTANCE = Mappers.getMapper(GoalCommandMapper.class);
    Goal toGoal(GoalCreateCommand command);

    GoalCreateResult toGoalCreateResult(Long goalId);

    GoalModifyRequest toGoalModifyRequest(GoalModifyCommand command);

    List<GoalCompleteNotificationDto> toGoalCompleteNotificationDtos(List<TeamMate> teamMates);

    @Mappings({
            @Mapping(source = "teamMate.userId", target = "userId"),
            @Mapping(source = "teamMate.goal.id", target = "goalId"),
            @Mapping(source = "teamMate.goal.title", target = "goalTitle")
    })
    GoalCompleteNotificationDto toGoalCompleteNotificationDto(TeamMate teamMate);
}
