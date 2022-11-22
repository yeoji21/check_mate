package checkmate.goal.application.dto;

import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.domain.*;
import checkmate.notification.domain.factory.dto.CompleteGoalNotificationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoalCommandMapper {
    GoalCommandMapper INSTANCE = Mappers.getMapper(GoalCommandMapper.class);

    @Mappings({
            @Mapping(target = "checkDays", source = "checkDays", qualifiedByName = "checkDays"),
            @Mapping(target = "period", source = "command", qualifiedByName = "goalPeriod")
    })
    Goal toGoal(GoalCreateCommand command);

    @Named("checkDays")
    default GoalCheckDays checkDays(String checkDays) {
        return new GoalCheckDays(checkDays);
    }

    @Named("goalPeriod")
    default GoalPeriod period(GoalCreateCommand command) {
        return new GoalPeriod(command.getStartDate(), command.getEndDate());
    }

    GoalModifyRequest toGoalModifyRequest(GoalModifyCommand command);

    List<CompleteGoalNotificationDto> toGoalCompleteNotificationDtos(List<TeamMate> teamMates);
}
