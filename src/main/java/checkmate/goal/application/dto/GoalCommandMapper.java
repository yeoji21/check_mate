package checkmate.goal.application.dto;

import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCheckDays;
import checkmate.goal.domain.GoalModifyEvent;
import checkmate.goal.domain.GoalPeriod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GoalCommandMapper {

    GoalCommandMapper INSTANCE = Mappers.getMapper(GoalCommandMapper.class);

    @Mappings({
        @Mapping(target = "checkDays", source = "checkDays", qualifiedByName = "checkDays"),
        @Mapping(target = "period", source = "command", qualifiedByName = "goalPeriod")
    })
    Goal toEntity(GoalCreateCommand command);

    @Named("checkDays")
    default GoalCheckDays checkDays(String checkDays) {
        return new GoalCheckDays(checkDays);
    }

    @Named("goalPeriod")
    default GoalPeriod period(GoalCreateCommand command) {
        return new GoalPeriod(command.startDate(), command.endDate());
    }

    GoalModifyEvent toModifyEvent(GoalModifyCommand command);
}
