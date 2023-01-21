package checkmate.goal.application;

import checkmate.TestEntityFactory;
import checkmate.common.cache.CacheTemplate;
import checkmate.goal.application.dto.TeamMateCommandMapper;
import checkmate.goal.application.dto.request.TeamMateInviteReplyCommand;
import checkmate.goal.application.dto.request.TeamMateInviteCommand;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TeamMateCommandServiceTest {
    @Mock private GoalRepository goalRepository;
    @Mock private UserRepository userRepository;
    @Mock private TeamMateRepository teamMateRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private CacheTemplate cacheTemplate;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Spy private TeamMateCommandMapper commandMapper = TeamMateCommandMapper.INSTANCE;
    @InjectMocks
    private TeamMateCommandService teamMateCommandService;

    @Test @DisplayName("초대를 거절한 적이 있는 유저에게 초대")
    void inviteTeamMate() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        User inviter = TestEntityFactory.user(1L, "inviter");
        User invitee = TestEntityFactory.user(5L, "invitee");

        TeamMate inviteeTeamMate = goal.join(invitee);
        ReflectionTestUtils.setField(inviteeTeamMate, "status", TeamMateStatus.REJECT);

        given(goalRepository.findById(any(Long.class))).willReturn(Optional.of(goal));
        given(userRepository.findByNickname(any(String.class))).willReturn(Optional.of(invitee));
        given(teamMateRepository.findTeamMateWithGoal(any(Long.class), any(Long.class))).willReturn(Optional.of(inviteeTeamMate));
        given(userRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(inviter));

        TeamMateInviteCommand command = TeamMateInviteCommand.builder()
                .goalId(goal.getId())
                .inviterUserId(inviter.getId())
                .inviteeNickname(invitee.getNickname())
                .build();

        //when
        teamMateCommandService.inviteTeamMate(command);

        //then
        assertThat(inviteeTeamMate.getStatus()).isEqualTo(TeamMateStatus.WAITING);
        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test @DisplayName("팀원 초대 수락")
    void inviteAccpet() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        User inviter = TestEntityFactory.user(1L, "inviter");
        User invitee = TestEntityFactory.user(2L, "invitee");
        TeamMate teamMate = goal.join(invitee);
        ReflectionTestUtils.setField(teamMate, "id", 1L);

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

        given(notificationRepository.findNotificationReceiver(any(Long.class), any(Long.class)))
                .willReturn(Optional.of(receiver));
        given(teamMateRepository.findTeamMateWithGoal(any(Long.class))).willReturn(Optional.of(teamMate));
        given(goalRepository.countOngoingGoals(any(Long.class))).willReturn(7);
        given(userRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(invitee));

        //when
        TeamMateAcceptResult result = teamMateCommandService.inviteAccept(new TeamMateInviteReplyCommand(invitee.getId(), notification.getId()));

        //then
        assertThat(result.getGoalId()).isEqualTo(goal.getId());
        assertThat(result.getTeamMateId()).isEqualTo(teamMate.getId());
        assertThat(teamMate.getStatus()).isEqualTo(TeamMateStatus.ONGOING);
        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test @DisplayName("팀원 초대 거절")
    void inviteReject() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        User inviter = TestEntityFactory.user(1L, "inviter");
        User invitee = TestEntityFactory.user(2L, "invitee");
        TeamMate teamMate = goal.join(invitee);

        NotificationReceiver receiver = new NotificationReceiver(teamMate.getUserId());
        Notification notification = Notification.builder()
                .userId(inviter.getId())
                .type(NotificationType.INVITE_GOAL)
                .title("title")
                .content("content")
                .receivers(List.of(receiver))
                .build();
        notification.addAttribute("teamMateId", 1L);
        ReflectionTestUtils.setField(notification, "id", 1L);

        given(notificationRepository.findNotificationReceiver(any(Long.class), any(Long.class)))
                .willReturn(Optional.of(receiver));
        given(teamMateRepository.findTeamMateWithGoal(any(Long.class))).willReturn(Optional.of(teamMate));
        given(userRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(invitee));

        //when
        teamMateCommandService.inviteReject(new TeamMateInviteReplyCommand(invitee.getId(), notification.getId()));

        //then
        assertThat(teamMate.getStatus()).isEqualTo(TeamMateStatus.REJECT);
        assertThat(receiver.isChecked()).isTrue();
        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test @DisplayName("인증일에 인증하지 않은 팀원 업데이트")
    void updateHookyTeamMate() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        List<TeamMate> hookyTms = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            hookyTms.add(goal.join(TestEntityFactory.user((long) i, "user" + i)));
        }

        given(teamMateRepository.updateYesterdayHookyTMs()).willReturn(hookyTms);
        given(teamMateRepository.eliminateOveredTMs(hookyTms)).willReturn(Collections.EMPTY_LIST);

        //when
        teamMateCommandService.updateHookyTeamMate();

        //then
        verify(cacheTemplate).deleteTMCacheData(any(List.class));
        verify(eventPublisher).publishEvent(any(NotPushNotificationCreatedEvent.class));
    }
}
