package checkmate.notification.application;

import static org.assertj.core.api.Assertions.assertThat;

import checkmate.notification.application.dto.NotificationQueryMapper;
import checkmate.notification.application.dto.response.NotificationAttributeInfo;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationAttributeKey;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.infrastructure.FakeNotificationRepository;
import checkmate.notification.infrastructure.NotificationQueryDao;
import checkmate.notification.presentation.dto.NotificationAttributeInfoResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Spy
    private NotificationRepository notificationRepository = new FakeNotificationRepository();
    @Mock
    private NotificationQueryDao notificationQueryDao;
    @Spy
    private NotificationQueryMapper mapper = NotificationQueryMapper.INSTANCE;
    @InjectMocks
    private NotificationQueryService notificationQueryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mapper, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("단건 알림 조회")
    void findNotificationReceiver() throws Exception {
        //given
        NotificationReceiver receiver = new NotificationReceiver(1L);
        Notification notification = createAndSaveNotification(receiver,
            NotificationType.POST_UPLOAD);
        notification.addAttribute(NotificationAttributeKey.GOAL_ID, 1L);

        //when
        NotificationAttributeInfo info = notificationQueryService.findNotificationInfo(1L,
            receiver.getUserId());
        HashMap<String, Long> attributes = objectMapper.readValue(info.getAttributes(),
            HashMap.class);

        //then
        assertThat(receiver.isRead()).isTrue();
        assertThat(attributes.containsKey(NotificationAttributeKey.GOAL_ID.getKey())).isTrue();
        assertThat(info.getTitle()).isEqualTo(notification.getTitle());
        assertThat(info.getContent()).isEqualTo(notification.getContent());
    }

    @Test
    @DisplayName("단건 알림 조회 - 목표 초대 알림")
    void findNotificationReceiver_invite_goal() throws Exception {
        //given
        NotificationReceiver receiver = new NotificationReceiver(1L);
        Notification notification = createAndSaveNotification(receiver,
            NotificationType.INVITE_SEND);

        //when
        NotificationAttributeInfo info = notificationQueryService.findNotificationInfo(
            notification.getId(),
            receiver.getUserId());

        //then
        assertThat(receiver.isRead()).isFalse();
        assertThat(info.getTitle()).isEqualTo(notification.getTitle());
        assertThat(info.getContent()).isEqualTo(notification.getContent());
    }

    @Test
    @DisplayName("목표 완료 팀원 알림 정보 조회")
    void findGoalCompleteNotifications() throws Exception {
        //given
        List<NotificationReceiver> receivers = createAndSaveNotificationReceivers();

        //when
        NotificationAttributeInfoResult result = notificationQueryService.findGoalCompleteNotifications(
            1L);

        //then
        assertThat(result.getNotifications().size()).isEqualTo(receivers.size());
        assertThat(receivers).allMatch(receiver -> receiver.isRead());
        assertThat(result.getNotifications()).allMatch(
            noti -> noti.getType().equals(NotificationType.COMPLETE_GOAL.name()));
    }

    private List<NotificationReceiver> createAndSaveNotificationReceivers() {
        List<NotificationReceiver> receivers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            NotificationReceiver receiver = new NotificationReceiver(1);
            receivers.add(receiver);
            Notification notification = Notification.builder()
                .title("title")
                .content("content")
                .type(NotificationType.COMPLETE_GOAL)
                .userId(1L)
                .receivers(List.of(receiver))
                .build();
            notificationRepository.save(notification);
        }
        return receivers;
    }

    private Notification createAndSaveNotification(NotificationReceiver receiver,
        NotificationType type) {
        Notification notification = Notification.builder()
            .userId(1L)
            .title("title")
            .content("body")
            .type(type)
            .receivers(List.of(receiver))
            .build();
        notificationRepository.save(notification);
        return notification;
    }
}