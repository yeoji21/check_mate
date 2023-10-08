package checkmate.mate.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import checkmate.TestEntityFactory;
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
import checkmate.mate.infra.FakeMateRepository;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationAttributeKey;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.notification.infrastructure.FakeNotificationRepository;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import checkmate.user.infrastructure.FakeUserRepository;
import java.util.List;
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
    @Spy
    private NotificationRepository notificationRepository = new FakeNotificationRepository();
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Spy
    private MateCommandMapper commandMapper = MateCommandMapper.INSTANCE;
    @InjectMocks
    private MateCommandService mateCommandService;

    @Test
    void invite_to_new_mate() throws Exception {
        //given
        Goal goal = createAndSaveGoal();
        User invitee = createAndSaveUser();
        MateInviteCommand command = createMateInviteCommand(goal, invitee);

        //when
        mateCommandService.sendInvite(command);

        //then
        verify(mateRepository).save(any());
        assertThat(findMate(command.goalId(), invitee.getId()).getStatus()).isEqualTo(
            MateStatus.WAITING);
        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("초대를 거절한 적이 있는 유저 초대")
    void invite_to_rejected_status_mate() throws Exception {
        //given
        Goal goal = createAndSaveGoal();
        User invitee = createAndSaveUser();
        Mate inviteeMate = createRejectedMate(goal, invitee);
        MateInviteCommand command = createMateInviteCommand(goal, invitee);

        //when
        mateCommandService.sendInvite(command);

        //then
        verify(goalRepository, never()).find(anyLong());
        assertThat(inviteeMate.getStatus()).isEqualTo(MateStatus.WAITING);
        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("팀원 초대 수락")
    void inviteAccpet() throws Exception {
        //given
        Mate inviteeMate = createInvitedMate();
        Notification notification = createAndSaveInviteNotification(inviteeMate);

        MateInviteReplyCommand command = new MateInviteReplyCommand(inviteeMate.getUserId(),
            notification.getId());

        //when
        MateAcceptResult result = mateCommandService.acceptInvite(command);

        //then
        assertThat(inviteeMate.getStatus()).isEqualTo(MateStatus.ONGOING);
        assertThat(result.goalId()).isEqualTo(inviteeMate.getGoal().getId());
        assertThat(result.mateId()).isEqualTo(inviteeMate.getId());
        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("팀원 초대 거절")
    void inviteReject() throws Exception {
        //given
        Mate inviteeMate = createAndSaveMate();
        ReflectionTestUtils.setField(inviteeMate, "status", MateStatus.WAITING);
        Notification notification = createAndSaveInviteNotification(inviteeMate);
        NotificationReceiver receiver = notification.getReceivers().get(0);

        //when
        mateCommandService.rejectInvite(
            new MateInviteReplyCommand(inviteeMate.getUserId(), notification.getId()));

        //then
        assertThat(inviteeMate.getStatus()).isEqualTo(MateStatus.REJECT);
        assertThat(receiver.isRead()).isTrue();

        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    private Mate createInvitedMate() {
        Mate inviteeMate = createAndSaveMate();
        inviteeMate.receiveInvite();
        return inviteeMate;
    }

    private Notification createAndSaveInviteNotification(Mate mate) {
        User inviter = createAndSaveUser();
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
        notificationRepository.save(notification);
        return notification;
    }

    private Mate findMate(long goalId, long userId) {
        return mateRepository.findWithGoal(goalId, userId).orElseThrow();
    }

    private Mate createAndSaveMate() {
        return createAndSaveMate(createAndSaveGoal(), createAndSaveUser());
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

    private Mate createRejectedMate(Goal goal, User user) {
        Mate inviteeMate = createAndSaveMate(goal, user);
        ReflectionTestUtils.setField(inviteeMate, "status", MateStatus.REJECT);
        return inviteeMate;
    }

    private MateInviteCommand createMateInviteCommand(Goal goal, User invitee) {
        return MateInviteCommand.builder()
            .goalId(goal.getId())
            .inviterUserId(createAndSaveUser().getId())
            .inviteeNickname(invitee.getNickname())
            .build();
    }
}
