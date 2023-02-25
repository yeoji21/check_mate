package checkmate.notification.application;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.notification.domain.factory.NotificationGenerator;
import checkmate.notification.domain.factory.dto.ExpulsionGoalNotificationDto;
import checkmate.notification.domain.factory.dto.NotificationCreateDto;
import checkmate.notification.domain.factory.dto.PostUploadNotificationDto;
import checkmate.notification.infrastructure.PushNotificationSender;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static checkmate.notification.domain.NotificationType.COMPLETE_GOAL;
import static checkmate.notification.domain.NotificationType.POST_UPLOAD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationPushServiceTest {
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationGenerator notificationGenerator;
    @Mock
    private PushNotificationSender pushNotificationSender;
    @InjectMocks
    private NotificationPushService notificationPushService;

    @Test
    void 전송_알림_테스트() throws Exception {
        //given
        PostUploadNotificationDto dto = getNotificationCommand();
        Notification notification = TestEntityFactory.notification(1L, 1L, POST_UPLOAD);

        given(notificationGenerator.generate(any(NotificationType.class), any(NotificationCreateDto.class))).willReturn(notification);
        doAnswer((invocation -> {
            Notification argument = (Notification) invocation.getArgument(0);
            ReflectionTestUtils.setField(argument, "id", 1L);
            return argument;
        })).when(notificationRepository).save(notification);
        given(notificationRepository.findReceiversFcmToken(any(Long.class))).willReturn(List.of("1", "2", "3"));

        //when
        notificationPushService.push(POST_UPLOAD, dto);

        //then
        verify(notificationRepository).save(any(Notification.class));
        verify(pushNotificationSender).send(any(Notification.class), any(List.class));
    }

    @Test
    void 전송하지_않는_알림_테스트() throws Exception {
        //given
        List<ExpulsionGoalNotificationDto> dto = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dto.add(new ExpulsionGoalNotificationDto(i, i, "title"));
        }

        NotPushNotificationCreatedEvent event = new NotPushNotificationCreatedEvent(COMPLETE_GOAL, dto);

        //when
        notificationPushService.notPush(NotificationType.EXPULSION_GOAL, dto);

        //then
        verify(notificationRepository).saveAll(any(List.class));
    }

    private PostUploadNotificationDto getNotificationCommand() {
        User user = TestEntityFactory.user(1L, "user");
        Goal goal = TestEntityFactory.goal(1L, "goal");

        return PostUploadNotificationDto.builder()
                .uploaderUserId(user.getId())
                .uploaderNickname(user.getNickname())
                .goalId(goal.getId())
                .goalTitle(goal.getTitle())
                .mateUserIds(List.of(1L, 2L))
                .build();
    }
}