package checkmate.notification.application.dto;

import checkmate.notification.application.dto.response.NotificationInfo;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationAttributeConverter;
import checkmate.notification.domain.NotificationType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface NotificationQueryMapper {
    NotificationQueryMapper INSTANCE = Mappers.getMapper(NotificationQueryMapper.class);

    @Mappings({
            @Mapping(target = "type", source = "notification.type", qualifiedByName = "getNotificationType"),
            @Mapping(target = "attributes", source = "notification", qualifiedByName = "getAttributes")
    })
    NotificationInfo toInfo(Notification notification);

    @Named("getNotificationType")
    default String getNotificationType(NotificationType type) {
        return type.name();
    }

    @Named("getAttributes")
    default String getAttributes(Notification notification) {
        return NotificationAttributeConverter.attributesToJson(notification);
    }
}
