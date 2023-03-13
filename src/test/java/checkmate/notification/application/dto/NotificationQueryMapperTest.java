package checkmate.notification.application.dto;

import checkmate.MapperTest;
import checkmate.TestEntityFactory;
import checkmate.notification.application.dto.response.NotificationAttributeInfo;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class NotificationQueryMapperTest extends MapperTest {
    private static final NotificationQueryMapper mapper = NotificationQueryMapper.INSTANCE;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mapper, "objectMapper", objectMapper);
    }

    @Test
    void notificationInfo() throws Exception {
        //given
        Notification notification = TestEntityFactory.notification(1L, 2L, NotificationType.POST_UPLOAD);
        notification.addAttribute("key1", "value1");
        notification.addAttribute("key2", "value2");
        notification.addAttribute("key3", "value3");

        //when
        NotificationAttributeInfo info = mapper.toInfo(notification);

        //then
        isEqualTo(info.getTitle(), notification.getTitle());
        isEqualTo(info.getContent(), notification.getContent());
        isEqualTo(info.getType(), notification.getType().name());
        isEqualTo(info.getAttributes(), new ObjectMapper().writeValueAsString(notification.getAttributes()));
    }
}