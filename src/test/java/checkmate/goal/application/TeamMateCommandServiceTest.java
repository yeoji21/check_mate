package checkmate.goal.application;

import checkmate.TestEntityFactory;
import checkmate.common.cache.CacheHandler;
import checkmate.goal.application.dto.TeamMateCommandMapper;
import checkmate.goal.application.dto.request.TeamMateInviteCommand;
import checkmate.goal.application.dto.request.TeamMateInviteReplyCommand;
import checkmate.goal.application.dto.response.TeamMateAcceptResult;
import checkmate.goal.domain.*;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TeamMateCommandServiceTest {
    @Mock
    private GoalRepository goalRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TeamMateRepository teamMateRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private TeamMateInitiateManager teamMateInitiateManager;
    @Mock
    private CacheHandler cacheHandler;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Spy
    private TeamMateCommandMapper commandMapper = TeamMateCommandMapper.INSTANCE;
    @InjectMocks
    private TeamMateCommandService teamMateCommandService;

    @Test
    @DisplayName("초대를 거절한 적이 있는 유저에게 초대")
    void inviteTeamMate() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        User inviter = TestEntityFactory.user(1L, "inviter");
        User invitee = TestEntityFactory.user(5L, "invitee");
        TeamMate inviteeTeamMate = getRejectStatusTeamMate(goal, invitee);

        TeamMateInviteCommand command = getTeamMateInviteCommand(goal, inviter, invitee);

        given(goalRepository.findById(any(Long.class))).willReturn(Optional.of(goal));
        given(userRepository.findByNickname(any(String.class))).willReturn(Optional.of(invitee));
        given(teamMateRepository.findTeamMateWithGoal(any(Long.class), any(Long.class))).willReturn(Optional.of(inviteeTeamMate));
        given(userRepository.findNicknameById(any(Long.class))).willReturn(Optional.ofNullable(inviter.getNickname()));

        //when
        teamMateCommandService.inviteTeamMate(command);

        //then
        assertThat(inviteeTeamMate.getStatus()).isEqualTo(TeamMateStatus.WAITING);

        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("팀원 초대 수락")
    void inviteAccpet() throws Exception {
        //given
        User invitee = TestEntityFactory.user(2L, "invitee");
        TeamMate inviteeTeamMate = getTeamMate(invitee);
        Notification notification = getInviteNotification(inviteeTeamMate);

        TeamMateInviteReplyCommand command = new TeamMateInviteReplyCommand(invitee.getId(), notification.getId());
        NotificationReceiver receiver = notification.getReceivers().get(0);

        given(notificationRepository.findNotificationReceiver(any(Long.class), any(Long.class)))
                .willReturn(Optional.of(receiver));
        given(teamMateRepository.findTeamMateWithGoal(any(Long.class))).willReturn(Optional.of(inviteeTeamMate));
        given(userRepository.findNicknameById(any(Long.class))).willReturn(Optional.ofNullable(invitee.getNickname()));
        doAnswer((invocation) -> {
            TeamMate argument = (TeamMate) invocation.getArgument(0);
            ReflectionTestUtils.setField(argument, "status", TeamMateStatus.ONGOING);
            return argument;
        }).when(teamMateInitiateManager).initiate(any(TeamMate.class));

        //when
        TeamMateAcceptResult result = teamMateCommandService.inviteAccept(command);

        //then
        assertThat(result.goalId()).isEqualTo(inviteeTeamMate.getGoal().getId());
        assertThat(result.teamMateId()).isEqualTo(inviteeTeamMate.getId());
        assertThat(inviteeTeamMate.getStatus()).isEqualTo(TeamMateStatus.ONGOING);

        verify(teamMateInitiateManager).initiate(any(TeamMate.class));
        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("팀원 초대 거절")
    void inviteReject() throws Exception {
        //given
        User invitee = TestEntityFactory.user(2L, "invitee");
        TeamMate inviteeTeamMate = getTeamMate(invitee);
        Notification notification = getInviteNotification(inviteeTeamMate);
        NotificationReceiver receiver = notification.getReceivers().get(0);

        given(notificationRepository.findNotificationReceiver(any(Long.class), any(Long.class)))
                .willReturn(Optional.of(receiver));
        given(teamMateRepository.findTeamMateWithGoal(any(Long.class))).willReturn(Optional.of(inviteeTeamMate));
        given(userRepository.findNicknameById(any(Long.class))).willReturn(Optional.ofNullable(invitee.getNickname()));

        //when
        teamMateCommandService.inviteReject(new TeamMateInviteReplyCommand(invitee.getId(), notification.getId()));

        //then
        assertThat(inviteeTeamMate.getStatus()).isEqualTo(TeamMateStatus.REJECT);
        assertThat(receiver.isChecked()).isTrue();

        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("인증일에 인증하지 않은 팀원 업데이트")
    void updateHookyTeamMate() throws Exception {
        //given
        List<TeamMate> hookyTeamMates = getHookyTeamMates();
        given(teamMateRepository.updateYesterdayHookyTMs()).willReturn(hookyTeamMates);
        given(teamMateRepository.eliminateOveredTMs(hookyTeamMates)).willReturn(Collections.EMPTY_LIST);

        //when
        teamMateCommandService.updateHookyTeamMate();

        //then
        verify(cacheHandler).deleteTeamMateCaches(any(List.class));
        verify(eventPublisher).publishEvent(any(NotPushNotificationCreatedEvent.class));
    }

    private Notification getInviteNotification(TeamMate teamMate) {
        User inviter = TestEntityFactory.user(1L, "inviter");
        NotificationReceiver receiver = new NotificationReceiver(teamMate.getUserId());
        Notification notification = Notification.builder()
                .userId(inviter.getId())
                .type(NotificationType.INVITE_GOAL)
                .title("title")
                .content("content")
                .receivers(List.of(receiver))
                .build();
        notification.addAttribute("teamMateId", teamMate.getId());
        ReflectionTestUtils.setField(notification, "id", 1L);
        return notification;
    }

    private List<TeamMate> getHookyTeamMates() {
        Goal goal = TestEntityFactory.goal(1L, "goal");
        List<TeamMate> hookyTms = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            hookyTms.add(goal.join(TestEntityFactory.user((long) i, "user" + i)));
        }
        return hookyTms;
    }

    private TeamMate getTeamMate(User user) {
        TeamMate teamMate = TestEntityFactory.goal(1L, "자바의 정석 스터디").join(user);
        ReflectionTestUtils.setField(teamMate, "id", 1L);
        return teamMate;
    }

    private TeamMate getRejectStatusTeamMate(Goal goal, User user) {
        TeamMate inviteeTeamMate = goal.join(user);
        ReflectionTestUtils.setField(inviteeTeamMate, "status", TeamMateStatus.REJECT);
        return inviteeTeamMate;
    }

    private TeamMateInviteCommand getTeamMateInviteCommand(Goal goal, User inviter, User invitee) {
        return TeamMateInviteCommand.builder()
                .goalId(goal.getId())
                .inviterUserId(inviter.getId())
                .inviteeNickname(invitee.getNickname())
                .build();
    }
}
