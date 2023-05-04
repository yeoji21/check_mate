package checkmate.notification.application;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.NotificationSender;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.NotificationGenerator;
import checkmate.notification.domain.factory.dto.ExpulsionGoalNotificationDto;
import checkmate.notification.domain.factory.dto.NotificationCreateDto;
import checkmate.notification.domain.factory.dto.PostUploadNotificationDto;
import checkmate.notification.infrastructure.NotificationQueryDao;
import checkmate.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static checkmate.notification.domain.NotificationType.POST_UPLOAD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationCommandServiceTest {
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationQueryDao notificationQueryDao;
    @Mock
    private NotificationGenerator notificationGenerator;
    @Mock
    private NotificationSender notificationSender;
    @InjectMocks
    private NotificationCommandService notificationCommandService;

    @Test
    @DisplayName("푸쉬 알림 생성")
    void savePushNotification() throws Exception {
        //given
        PostUploadNotificationDto dto = createNotificationCommand();
        Notification notification = TestEntityFactory.notification(1L, 1L, POST_UPLOAD);

        given(notificationGenerator.generate(any(NotificationType.class), any(NotificationCreateDto.class))).willReturn(notification);
        doAnswer((invocation -> {
            Notification argument = (Notification) invocation.getArgument(0);
            ReflectionTestUtils.setField(argument, "id", 1L);
            return argument;
        })).when(notificationRepository).save(notification);
        given(notificationQueryDao.findReceiversFcmToken(any(Long.class))).willReturn(List.of("token1", "token2", "token3"));

        //when
        notificationCommandService.savePushNotification(POST_UPLOAD, dto);

        //then
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationSender).send(any(Notification.class), any(List.class));
    }

    @Test
    @DisplayName("전송하지 않는 알림 생성")
    void saveNotPushNotifications() throws Exception {
        //given
        List<ExpulsionGoalNotificationDto> dto = createExpulsionGoalNotificationDtos();

        //when
        notificationCommandService.saveNotPushNotifications(NotificationType.EXPULSION_GOAL, dto);

        //then
        verify(notificationRepository).saveAll(any(List.class));
    }

    private List<ExpulsionGoalNotificationDto> createExpulsionGoalNotificationDtos() {
        List<ExpulsionGoalNotificationDto> dto = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dto.add(new ExpulsionGoalNotificationDto(i, i, "title"));
        }
        return dto;
    }

    private PostUploadNotificationDto createNotificationCommand() {
        User user = TestEntityFactory.user(1L, "user");
        Goal goal = TestEntityFactory.goal(1L, "goal");

        PostUploadNotificationDto dto = PostUploadNotificationDto.builder()
                .uploaderUserId(user.getId())
                .uploaderNickname(user.getNickname())
                .goalId(goal.getId())
                .goalTitle(goal.getTitle())
                .build();
        dto.setMateUserIds(List.of(1L, 2L));
        return dto;
    }
}