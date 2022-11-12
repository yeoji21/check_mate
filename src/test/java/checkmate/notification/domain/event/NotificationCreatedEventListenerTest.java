package checkmate.notification.domain.event;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.NotificationGenerator;
import checkmate.notification.domain.factory.PostUploadNotificationFactory;
import checkmate.notification.domain.factory.dto.KickOutNotificationDto;
import checkmate.notification.domain.factory.dto.NotificationCreateDto;
import checkmate.notification.domain.factory.dto.PostUploadNotificationDto;
import checkmate.notification.domain.push.PushNotificationSender;
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
class NotificationCreatedEventListenerTest {
    @Mock private NotificationRepository repository;
    @Mock private NotificationGenerator generator;
    @Mock private PushNotificationSender pushNotificationSender;
    @InjectMocks private NotificationCreatedEventListener eventListener;

    @Test
    void 전송_알림_테스트() throws Exception{
        //given
        PostUploadNotificationDto command = getNotificationCommand();
        Notification notification = new PostUploadNotificationFactory().generate(command);

        given(generator.generate(any(NotificationType.class), any(NotificationCreateDto.class))).willReturn(notification);
        doAnswer((invocation -> {
            Notification argument = (Notification) invocation.getArgument(0);
            ReflectionTestUtils.setField(argument, "id", 1L);
            return argument;
        })).when(repository).save(notification);
        given(repository.findReceiversFcmToken(any(Long.class))).willReturn(List.of("1", "2", "3"));

        //when
        PushNotificationCreatedEvent event = new PushNotificationCreatedEvent(POST_UPLOAD, command);
        eventListener.pushNotification(event);

        //then
        verify(repository).save(any(Notification.class));
        verify(pushNotificationSender).send(any(Notification.class), any(List.class));
    }

    @Test
    void 전송하지_않는_알림_테스트() throws Exception{
        //given
        List<KickOutNotificationDto> command = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            command.add(new KickOutNotificationDto(i, i, "title"));
        }

        StaticNotificationCreatedEvent event = new StaticNotificationCreatedEvent(COMPLETE_GOAL, command);

        //when
        eventListener.staticNotification(event);

        //then
        verify(repository).saveAll(any(List.class));
    }

    private PostUploadNotificationDto getNotificationCommand() {
        User user = TestEntityFactory.user(1L, "user");
        Goal goal = TestEntityFactory.goal(1L, "goal");
        TeamMate teamMate = TestEntityFactory.teamMate(1L, user.getId());
        goal.addTeamMate(teamMate);

        return PostUploadNotificationDto.builder()
                .uploaderUserId(user.getId())
                .uploaderNickname(user.getNickname())
                .goalId(goal.getId())
                .goalTitle(goal.getTitle())
                .teamMateUserIds(List.of(1L, 2L))
                .build();
    }
}