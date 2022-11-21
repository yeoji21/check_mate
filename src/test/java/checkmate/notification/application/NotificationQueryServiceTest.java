package checkmate.notification.application;

import checkmate.notification.application.dto.NotificationQueryMapper;
import checkmate.notification.application.dto.response.NotificationInfo;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.infrastructure.NotificationQueryDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationQueryDao notificationQueryDao;
    @Spy private NotificationQueryMapper mapper = NotificationQueryMapper.INSTANCE;
    @InjectMocks private NotificationQueryService notificationQueryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 단건_알림_조회_테스트() throws Exception{
        //given
        NotificationReceiver receiver = new NotificationReceiver(1L);
        Notification notification = Notification.builder()
                .userId(1L)
                .title("title")
                .content("body")
                .type(NotificationType.POST_UPLOAD)
                .receivers(List.of(receiver))
                .build();
        notification.addAttribute("goalId", 1L);

        given(notificationRepository.findNotificationReceiver(any(Long.class), any(Long.class))).willReturn(Optional.of(receiver));

        //when
        NotificationInfo response = notificationQueryService.findNotificationInfo(1L, 2L);
        HashMap<String, Long> attributes = objectMapper.readValue(response.getAttributes(), HashMap.class);

        //then
        assertThat(receiver.isChecked()).isTrue();
        assertThat(attributes.containsKey("goalId")).isTrue();
        assertThat(response.getTitle()).isEqualTo(notification.getTitle());
        assertThat(response.getContent()).isEqualTo(notification.getContent());
    }
}