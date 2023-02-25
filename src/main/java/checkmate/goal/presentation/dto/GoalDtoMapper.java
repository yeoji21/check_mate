package checkmate.goal.presentation.dto;

import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.application.dto.request.LikeCountCreateCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GoalDtoMapper {
    GoalDtoMapper INSTANCE = Mappers.getMapper(GoalDtoMapper.class);

    @Mapping(source = "userId", target = "userId")
    GoalCreateCommand toCommand(GoalCreateDto dto, long userId);

    @Mappings({
            @Mapping(source = "userId", target = "userId"),
            @Mapping(source = "goalId", target = "goalId")
    })
    GoalModifyCommand toCommand(GoalModifyDto dto, long goalId, long userId);

    @Mappings({
            @Mapping(source = "dto.goalId", target = "goalId"),
            @Mapping(source = "dto.likeCount", target = "likeCount")
    })
    LikeCountCreateCommand toCommand(LikeCountCreateDto dto, long userId);
}
