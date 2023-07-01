package checkmate.mate.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import checkmate.TestEntityFactory;
import checkmate.common.cache.CacheHandler;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.infra.FakeGoalRepository;
import checkmate.mate.application.dto.MateCommandMapper;
import checkmate.mate.application.dto.request.MateInviteCommand;
import checkmate.mate.application.dto.request.MateInviteReplyCommand;
import checkmate.mate.application.dto.response.MateAcceptResult;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.Mate.MateStatus;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.domain.MateStartingService;
import checkmate.mate.infra.FakeMateRepository;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationAttributeKey;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import checkmate.user.infrastructure.FakeUserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MateCommandServiceTest {

    @Spy
    private GoalRepository goalRepository = new FakeGoalRepository();
    @Spy
    private UserRepository userRepository = new FakeUserRepository();
    @Spy
    private MateRepository mateRepository = new FakeMateRepository();
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private MateStartingService mateStartingService;
    @Mock
    private CacheHandler cacheHandler;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Spy
    private MateCommandMapper commandMapper = MateCommandMapper.INSTANCE;
    @InjectMocks
    private MateCommandService mateCommandService;

    @Test
    @DisplayName("유저 초대")
    void inviteTeamMate() throws Exception {
        //given
        User invitee = createAndSaveUser();
        User inviter = createAndSaveUser();
        Mate inviteeMate = createAndSaveMate(createAndSaveGoal(), invitee);

        MateInviteCommand command = createMateInviteCommand(invitee, inviteeMate, inviter);

        //when
        mateCommandService.sendInvite(command);

        //then
        assertThat(inviteeMate.getStatus()).isEqualTo(MateStatus.WAITING);
        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("초대를 거절한 적이 있는 유저 초대")
    void inviteTeamMate_rejected_status() throws Exception {
        //given
        User invitee = createAndSaveUser();
        User inviter = createAndSaveUser();
        Mate inviteeMate = createRejectStatusMate(invitee);
        MateInviteCommand command = createMateInviteCommand(invitee, inviteeMate, inviter);

        //when
        mateCommandService.sendInvite(command);

        //then
        assertThat(inviteeMate.getStatus()).isEqualTo(MateStatus.WAITING);
        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("팀원 초대 수락")
    void inviteAccpet() throws Exception {
        //given
        Mate inviteeMate = createMate();
        Notification notification = createInviteNotification(inviteeMate);

        MateInviteReplyCommand command = new MateInviteReplyCommand(inviteeMate.getUserId(),
            notification.getId());

        given(notificationRepository.findReceiver(any(Long.class), any(Long.class)))
            .willReturn(Optional.of(notification.getReceivers().get(0)));
        doAnswer((invocation) -> {
            Mate argument = (Mate) invocation.getArgument(0);
            ReflectionTestUtils.setField(argument, "status", MateStatus.ONGOING);
            return argument;
        }).when(mateStartingService).startToGoal(any(Mate.class));

        //when
        MateAcceptResult result = mateCommandService.acceptInvite(command);

        //then
        assertThat(inviteeMate.getStatus()).isEqualTo(MateStatus.ONGOING);
        assertThat(result.goalId()).isEqualTo(inviteeMate.getGoal().getId());
        assertThat(result.mateId()).isEqualTo(inviteeMate.getId());

        verify(mateStartingService).startToGoal(any(Mate.class));
        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("팀원 초대 거절")
    void inviteReject() throws Exception {
        //given
        Mate inviteeMate = createMate();
        ReflectionTestUtils.setField(inviteeMate, "status", MateStatus.WAITING);
        Notification notification = createInviteNotification(inviteeMate);
        NotificationReceiver receiver = notification.getReceivers().get(0);

        given(notificationRepository.findReceiver(any(Long.class), any(Long.class)))
            .willReturn(Optional.of(receiver));

        //when
        mateCommandService.rejectInvite(
            new MateInviteReplyCommand(inviteeMate.getUserId(), notification.getId()));

        //then
        assertThat(inviteeMate.getStatus()).isEqualTo(MateStatus.REJECT);
        assertThat(receiver.isChecked()).isTrue();

        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("인증일에 인증하지 않은 팀원 업데이트")
    void updateUploadSkippedMate() throws Exception {
        //given
        createUploadSkippedMates();

        //when
        mateCommandService.updateUploadSkippedMates();

        //then
        verify(cacheHandler).deleteUserCaches(any(List.class));
        verify(eventPublisher).publishEvent(any(NotPushNotificationCreatedEvent.class));
    }

    private Notification createInviteNotification(Mate mate) {
        User inviter = TestEntityFactory.user(1L, "inviter");
        NotificationReceiver receiver = new NotificationReceiver(mate.getUserId());
        Notification notification = Notification.builder()
            .userId(inviter.getId())
            .type(NotificationType.INVITE_SEND)
            .title("title")
            .content("content")
            .receivers(List.of(receiver))
            .build();
        notification.addAttribute(NotificationAttributeKey.MATE_ID, mate.getId());
        ReflectionTestUtils.setField(notification, "id", 1L);
        return notification;
    }

    private User createAndSaveUser() {
        return userRepository.save(TestEntityFactory.user(0L, "invitee"));
    }

    private Goal createAndSaveGoal() {
        return goalRepository.save(TestEntityFactory.goal(0L, "goal"));
    }

    private Mate createAndSaveMate(Goal goal, User user) {
        return mateRepository.save(goal.createMate(user));
    }

    private List<Mate> createUploadSkippedMates() {
        Goal goal = createAndSaveGoal();
        List<Mate> skippedMates = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            skippedMates.add(createAndSaveMate(goal, createAndSaveUser()));
        }
        return skippedMates;
    }

    private Mate createMate() {
        Mate mate = createAndSaveMate(TestEntityFactory.goal(1L, "자바의 정석 스터디"),
            createAndSaveUser());
        ReflectionTestUtils.setField(mate, "id", 1L);
        return mate;
    }

    private Mate createRejectStatusMate(User user) {
        Goal goal = TestEntityFactory.goal(1L, "goal");
        Mate inviteeMate = createAndSaveMate(goal, user);
        ReflectionTestUtils.setField(inviteeMate, "status", MateStatus.REJECT);
        return inviteeMate;
    }

    private MateInviteCommand createMateInviteCommand(User invitee, Mate inviteeMate,
        User inviter) {
        return MateInviteCommand.builder()
            .goalId(inviteeMate.getGoal().getId())
            .inviterUserId(inviter.getId())
            .inviteeNickname(invitee.getNickname())
            .build();
    }
}
