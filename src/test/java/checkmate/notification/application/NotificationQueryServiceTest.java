package checkmate.notification.application;

import checkmate.notification.application.dto.NotificationQueryMapper;
import checkmate.notification.application.dto.response.NotificationAttributeInfo;
import checkmate.notification.domain.*;
import checkmate.notification.infrastructure.NotificationQueryDao;
import checkmate.notification.presentation.dto.NotificationAttributeInfoResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private NotificationRepository notificationRepository;
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
        Notification notification = createNotification(receiver, NotificationType.POST_UPLOAD);
        notification.addAttribute(NotificationAttributeKey.GOAL_ID, 1L);

        given(notificationRepository.findReceiver(any(Long.class), any(Long.class))).willReturn(Optional.of(receiver));

        //when
        NotificationAttributeInfo info = notificationQueryService.findNotificationInfo(1L, 2L);
        HashMap<String, Long> attributes = objectMapper.readValue(info.getAttributes(), HashMap.class);

        //then
        assertThat(receiver.isChecked()).isTrue();
        assertThat(attributes.containsKey(NotificationAttributeKey.GOAL_ID.getKey())).isTrue();
        assertThat(info.getTitle()).isEqualTo(notification.getTitle());
        assertThat(info.getContent()).isEqualTo(notification.getContent());
    }

    @Test
    @DisplayName("단건 알림 조회 - 목표 초대 알림")
    void findNotificationReceiver_invite_goal() throws Exception {
        //given
        NotificationReceiver receiver = new NotificationReceiver(1L);
        Notification notification = createNotification(receiver, NotificationType.INVITE_GOAL);

        given(notificationRepository.findReceiver(any(Long.class), any(Long.class)))
                .willReturn(Optional.of(receiver));

        //when
        NotificationAttributeInfo info = notificationQueryService.findNotificationInfo(1L, 2L);

        //then
        assertThat(receiver.isChecked()).isFalse();
        assertThat(info.getTitle()).isEqualTo(notification.getTitle());
        assertThat(info.getContent()).isEqualTo(notification.getContent());
    }

    @Test
    @DisplayName("목표 완료 팀원 알림 정보 조회")
    void findGoalCompleteNotifications() throws Exception {
        //given
        List<NotificationReceiver> receivers = createNotificationReceivers();
        given(notificationRepository.findUnCheckedReceivers(anyLong(), any())).willReturn(receivers);

        //when
        NotificationAttributeInfoResult result = notificationQueryService.findGoalCompleteNotifications(1L);

        //then
        assertThat(result.getNotifications().size()).isEqualTo(receivers.size());
        assertThat(receivers).allMatch(receiver -> receiver.isChecked());
        assertThat(result.getNotifications()).allMatch(noti -> noti.getType().equals(NotificationType.COMPLETE_GOAL.name()));
    }

    private List<NotificationReceiver> createNotificationReceivers() {
        List<NotificationReceiver> receivers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            receivers.add(new NotificationReceiver(i));
        }
        Notification.builder()
                .title("title")
                .content("content")
                .type(NotificationType.COMPLETE_GOAL)
                .userId(1L)
                .receivers(receivers)
                .build();
        return receivers;
    }

    private Notification createNotification(NotificationReceiver receiver,
                                            NotificationType type) {
        return Notification.builder()
                .userId(1L)
                .title("title")
                .content("body")
                .type(type)
                .receivers(List.of(receiver))
                .build();
    }
}