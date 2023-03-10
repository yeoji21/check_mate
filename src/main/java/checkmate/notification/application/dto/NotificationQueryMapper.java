package checkmate.notification.application.dto;

import checkmate.notification.application.dto.response.NotificationAttributeInfo;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationAttributeConverter;
import checkmate.notification.domain.NotificationType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public abstract class NotificationQueryMapper {
    public static NotificationQueryMapper INSTANCE = Mappers.getMapper(NotificationQueryMapper.class);
    @Autowired
    NotificationAttributeConverter converter;

    @Mappings({
            @Mapping(target = "type", source = "notification.type", qualifiedByName = "getNotificationType"),
            @Mapping(target = "attributes", source = "notification", qualifiedByName = "getAttributes")
    })
    public abstract NotificationAttributeInfo toInfo(Notification notification);

    @Named("getNotificationType")
    String getNotificationType(NotificationType type) {
        return type.name();
    }

    @Named("getAttributes")
    String getAttributes(Notification notification) {
        return converter.convertToDatabaseColumn(notification.getAttributes());
    }
}
