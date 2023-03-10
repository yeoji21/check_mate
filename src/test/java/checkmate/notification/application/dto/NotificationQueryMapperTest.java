package checkmate.notification.application.dto;

import checkmate.MapperTest;
import checkmate.TestEntityFactory;
import checkmate.notification.application.dto.response.NotificationAttributeInfo;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationAttributeConverter;
import checkmate.notification.domain.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class NotificationQueryMapperTest extends MapperTest {
    private static final NotificationQueryMapper mapper = NotificationQueryMapper.INSTANCE;
    private NotificationAttributeConverter converter = new NotificationAttributeConverter();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mapper, "converter", converter);
    }

    @Test
    void notificationInfo() throws Exception {
        //given
        Notification notification = TestEntityFactory.notification(1L, 2L, NotificationType.POST_UPLOAD);
        notification.addAttribute("key", "value");

        //when
        NotificationAttributeInfo info = mapper.toInfo(notification);

        //then
        isEqualTo(info.getTitle(), notification.getTitle());
        isEqualTo(info.getContent(), notification.getContent());
        isEqualTo(info.getType(), notification.getType().name());
        isEqualTo(info.getAttributes(), converter.convertToDatabaseColumn(notification.getAttributes()));
    }
}