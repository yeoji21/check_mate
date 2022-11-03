package checkmate.notification.presentation.dto;

import checkmate.notification.application.dto.request.NotificationDetailsCriteria;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface NotificationDtoMapper{
    NotificationDtoMapper INSTANCE = Mappers.getMapper(NotificationDtoMapper.class);

    NotificationDetailsCriteria toDetailsCriteria(Long cursorId, Integer size, Long userId);
}
