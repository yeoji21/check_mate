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
import checkmate.mate.application.dto.MateCommandMapper;
import checkmate.mate.application.dto.request.MateInviteCommand;
import checkmate.mate.application.dto.request.MateInviteReplyCommand;
import checkmate.mate.application.dto.response.MateAcceptResult;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.Mate.MateStatus;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.domain.MateStartingService;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationAttributeKey;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
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
public class MateCommandServiceTest {

    @Mock
    private GoalRepository goalRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MateRepository mateRepository;
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
        User invitee = TestEntityFactory.user(5L, "invitee");
        Mate inviteeMate = TestEntityFactory.goal(1L, "goal").createMate(invitee);

        MateInviteCommand command = createMateInviteCommand(invitee, inviteeMate, 2L);

        given(goalRepository.findById(any(Long.class))).willReturn(
            Optional.of(inviteeMate.getGoal()));
        given(userRepository.findByNickname(any(String.class))).willReturn(Optional.of(invitee));
        given(mateRepository.findWithGoal(any(Long.class), any(Long.class))).willReturn(
            Optional.empty());
        given(mateRepository.save(any(Mate.class))).willReturn(inviteeMate);
        given(userRepository.findNicknameById(any(Long.class))).willReturn(Optional.of("inviter"));

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
        User invitee = TestEntityFactory.user(1L, "invitee");
        Mate inviteeMate = createRejectStatusMate(invitee);
        MateInviteCommand command = createMateInviteCommand(invitee, inviteeMate, 2L);

        given(userRepository.findByNickname(any(String.class))).willReturn(Optional.of(invitee));
        given(mateRepository.findWithGoal(any(Long.class), any(Long.class))).willReturn(
            Optional.of(inviteeMate));
        given(userRepository.findNicknameById(any(Long.class))).willReturn(Optional.of("inviter"));

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
        given(mateRepository.findById(any(Long.class))).willReturn(Optional.of(inviteeMate));
        given(userRepository.findNicknameById(any(Long.class))).willReturn(
            Optional.ofNullable("invitee"));
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
        given(mateRepository.findById(any(Long.class))).willReturn(Optional.of(inviteeMate));
        given(userRepository.findNicknameById(any(Long.class))).willReturn(
            Optional.ofNullable("invitee"));

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
        List<Mate> uploadSkippedMates = createUploadSkippedMates();

        //when
        mateCommandService.updateUploadSkippedMates();

        //then
        verify(mateRepository).increaseSkippedDayCount(any(List.class));
        verify(mateRepository).updateLimitOveredMates(any(List.class));
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

    private List<Mate> createUploadSkippedMates() {
        Goal goal = TestEntityFactory.goal(1L, "goal");
        List<Mate> skippedMates = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            skippedMates.add(goal.createMate(TestEntityFactory.user((long) i, "user" + i)));
        }
        return skippedMates;
    }

    private Mate createMate() {
        Mate mate = TestEntityFactory.goal(1L, "자바의 정석 스터디")
            .createMate(TestEntityFactory.user(1L, "invitee"));
        ReflectionTestUtils.setField(mate, "id", 1L);
        return mate;
    }

    private Mate createRejectStatusMate(User user) {
        Goal goal = TestEntityFactory.goal(1L, "goal");
        Mate inviteeMate = goal.createMate(user);
        ReflectionTestUtils.setField(inviteeMate, "status", MateStatus.REJECT);
        return inviteeMate;
    }

    private MateInviteCommand createMateInviteCommand(User invitee, Mate inviteeMate,
        long inviterUserId) {
        return MateInviteCommand.builder()
            .goalId(inviteeMate.getGoal().getId())
            .inviterUserId(inviterUserId)
            .inviteeNickname(invitee.getNickname())
            .build();
    }
}
