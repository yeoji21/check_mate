package checkmate.post.presentation.dto;

import checkmate.post.application.dto.request.PostCreateCommand;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PostDtoMapper {
    PostDtoMapper INSTANCE = Mappers.getMapper(PostDtoMapper.class);

    PostCreateCommand toCommand(PostCreateDto dto, long userId);
}
