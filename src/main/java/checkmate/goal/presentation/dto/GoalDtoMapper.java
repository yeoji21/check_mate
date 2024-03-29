package checkmate.goal.presentation.dto;

import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.application.dto.request.LikeCountCreateCommand;
import checkmate.goal.domain.CheckDaysConverter;
import java.time.DayOfWeek;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GoalDtoMapper {

    GoalDtoMapper INSTANCE = Mappers.getMapper(GoalDtoMapper.class);

    @Mappings({
        @Mapping(target = "checkDays", source = "dto.checkDays", qualifiedByName = "korToDayOfWeek"),
        @Mapping(source = "userId", target = "userId")
    })
    GoalCreateCommand toCommand(GoalCreateDto dto, long userId);

    @Named("korToDayOfWeek")
    default DayOfWeek[] checkDays(String checkDays) {
        return CheckDaysConverter.toDayOfWeeks(checkDays);
    }

    @Mappings({
        @Mapping(source = "userId", target = "userId"),
        @Mapping(source = "goalId", target = "goalId")
    })
    GoalModifyCommand toCommand(GoalModifyDto dto, long goalId, long userId);

    @Mappings({
        @Mapping(source = "dto.likeCount", target = "likeCount")
    })
    LikeCountCreateCommand toCommand(long goalId, LikeCountCreateDto dto, long userId);
}
