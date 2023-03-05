package checkmate.mate.application;

import checkmate.TestEntityFactory;
import checkmate.common.cache.CacheHandler;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.mate.application.dto.MateCommandMapper;
import checkmate.mate.application.dto.request.MateInviteCommand;
import checkmate.mate.application.dto.request.MateInviteReplyCommand;
import checkmate.mate.application.dto.response.MateAcceptResult;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateInitiateManager;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.domain.MateStatus;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

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
    private MateInitiateManager mateInitiateManager;
    @Mock
    private CacheHandler cacheHandler;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Spy
    private MateCommandMapper commandMapper = MateCommandMapper.INSTANCE;
    @InjectMocks
    private MateCommandService mateCommandService;

    @Test
    @DisplayName("초대를 거절한 적이 있는 유저에게 초대")
    void inviteTeamMate() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        User inviter = TestEntityFactory.user(1L, "inviter");
        User invitee = TestEntityFactory.user(5L, "invitee");
        Mate inviteeMate = getRejectStatusTeamMate(goal, invitee);

        MateInviteCommand command = getTeamMateInviteCommand(goal, inviter, invitee);

        given(goalRepository.findById(any(Long.class))).willReturn(Optional.of(goal));
        given(userRepository.findByNickname(any(String.class))).willReturn(Optional.of(invitee));
        given(mateRepository.findMateWithGoal(any(Long.class), any(Long.class))).willReturn(Optional.of(inviteeMate));
        given(userRepository.findNicknameById(any(Long.class))).willReturn(Optional.ofNullable(inviter.getNickname()));

        //when
        mateCommandService.inviteMate(command);

        //then
        assertThat(inviteeMate.getStatus()).isEqualTo(MateStatus.WAITING);

        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("팀원 초대 수락")
    void inviteAccpet() throws Exception {
        //given
        User invitee = TestEntityFactory.user(2L, "invitee");
        Mate inviteeMate = getTeamMate(invitee);
        Notification notification = getInviteNotification(inviteeMate);

        MateInviteReplyCommand command = new MateInviteReplyCommand(invitee.getId(), notification.getId());
        NotificationReceiver receiver = notification.getReceivers().get(0);

        given(notificationRepository.findNotificationReceiver(any(Long.class), any(Long.class)))
                .willReturn(Optional.of(receiver));
        given(mateRepository.findMateWithGoal(any(Long.class))).willReturn(Optional.of(inviteeMate));
        given(userRepository.findNicknameById(any(Long.class))).willReturn(Optional.ofNullable(invitee.getNickname()));
        doAnswer((invocation) -> {
            Mate argument = (Mate) invocation.getArgument(0);
            ReflectionTestUtils.setField(argument, "status", MateStatus.ONGOING);
            return argument;
        }).when(mateInitiateManager).initiate(any(Mate.class));

        //when
        MateAcceptResult result = mateCommandService.inviteAccept(command);

        //then
        assertThat(result.goalId()).isEqualTo(inviteeMate.getGoal().getId());
        assertThat(result.mateId()).isEqualTo(inviteeMate.getId());
        assertThat(inviteeMate.getStatus()).isEqualTo(MateStatus.ONGOING);

        verify(mateInitiateManager).initiate(any(Mate.class));
        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("팀원 초대 거절")
    void inviteReject() throws Exception {
        //given
        User invitee = TestEntityFactory.user(2L, "invitee");
        Mate inviteeMate = getTeamMate(invitee);
        Notification notification = getInviteNotification(inviteeMate);
        NotificationReceiver receiver = notification.getReceivers().get(0);

        given(notificationRepository.findNotificationReceiver(any(Long.class), any(Long.class)))
                .willReturn(Optional.of(receiver));
        given(mateRepository.findMateWithGoal(any(Long.class))).willReturn(Optional.of(inviteeMate));
        given(userRepository.findNicknameById(any(Long.class))).willReturn(Optional.ofNullable(invitee.getNickname()));

        //when
        mateCommandService.inviteReject(new MateInviteReplyCommand(invitee.getId(), notification.getId()));

        //then
        assertThat(inviteeMate.getStatus()).isEqualTo(MateStatus.REJECT);
        assertThat(receiver.isChecked()).isTrue();

        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("인증일에 인증하지 않은 팀원 업데이트")
    void updateHookyTeamMate() throws Exception {
        //given
        List<Mate> hookyMates = getHookyTeamMates();
        given(mateRepository.updateYesterdayHookyMates()).willReturn(hookyMates);

        //when
        mateCommandService.updateHookyMates();

        //then
        verify(mateRepository).eliminateOveredMates(any(List.class));
        verify(cacheHandler).deleteMateCaches(any(List.class));
        verify(eventPublisher).publishEvent(any(NotPushNotificationCreatedEvent.class));
    }

    private Notification getInviteNotification(Mate mate) {
        User inviter = TestEntityFactory.user(1L, "inviter");
        NotificationReceiver receiver = new NotificationReceiver(mate.getUserId());
        Notification notification = Notification.builder()
                .userId(inviter.getId())
                .type(NotificationType.INVITE_GOAL)
                .title("title")
                .content("content")
                .receivers(List.of(receiver))
                .build();
        notification.addAttribute("mateId", mate.getId());
        ReflectionTestUtils.setField(notification, "id", 1L);
        return notification;
    }

    private List<Mate> getHookyTeamMates() {
        Goal goal = TestEntityFactory.goal(1L, "goal");
        List<Mate> hookyTms = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            hookyTms.add(goal.join(TestEntityFactory.user((long) i, "user" + i)));
        }
        return hookyTms;
    }

    private Mate getTeamMate(User user) {
        Mate mate = TestEntityFactory.goal(1L, "자바의 정석 스터디").join(user);
        ReflectionTestUtils.setField(mate, "id", 1L);
        return mate;
    }

    private Mate getRejectStatusTeamMate(Goal goal, User user) {
        Mate inviteeMate = goal.join(user);
        ReflectionTestUtils.setField(inviteeMate, "status", MateStatus.REJECT);
        return inviteeMate;
    }

    private MateInviteCommand getTeamMateInviteCommand(Goal goal, User inviter, User invitee) {
        return MateInviteCommand.builder()
                .goalId(goal.getId())
                .inviterUserId(inviter.getId())
                .inviteeNickname(invitee.getNickname())
                .build();
    }
}
