package checkmate.post.presentation.dto;

import checkmate.post.application.dto.request.PostUploadCommand;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PostDtoMapper {
    PostDtoMapper INSTANCE = Mappers.getMapper(PostDtoMapper.class);

    PostUploadCommand toCommand(PostUploadDto dto, long userId);
}
