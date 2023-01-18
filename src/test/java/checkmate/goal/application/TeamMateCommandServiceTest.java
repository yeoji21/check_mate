package checkmate.goal.application;

import checkmate.TestEntityFactory;
import checkmate.common.cache.CacheTemplate;
import checkmate.goal.application.dto.TeamMateCommandMapper;
import checkmate.goal.application.dto.request.InviteReplyCommand;
import checkmate.goal.application.dto.request.TeamMateInviteReplyCommand;
import checkmate.goal.application.dto.response.TeamMateInviteReplyResult;
import checkmate.goal.domain.*;
import checkmate.goal.presentation.dto.TeamMateDtoMapper;
import checkmate.goal.presentation.dto.request.TeamMateInviteDto;
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
    @Spy private TeamMateDtoMapper dtoMapper = TeamMateDtoMapper.INSTANCE;
    @InjectMocks
    private TeamMateCommandService teamMateCommandService;

    @Test @DisplayName("초대를 거절한 적이 있는 유저에게 초대")
    void inviteTeamMate() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        User inviter = TestEntityFactory.user(1L, "inviter");
        User invitee = TestEntityFactory.user(5L, "invitee");
        TeamMateInviteDto dto = new TeamMateInviteDto(1L, invitee.getNickname());

        TeamMate inviteeTeamMate = goal.join(invitee);
        ReflectionTestUtils.setField(inviteeTeamMate, "status", TeamMateStatus.REJECT);

        given(goalRepository.findById(any(Long.class))).willReturn(Optional.of(goal));
        given(userRepository.findByNickname(any(String.class))).willReturn(Optional.of(invitee));
        given(teamMateRepository.findTeamMateWithGoal(any(Long.class), any(Long.class))).willReturn(Optional.of(inviteeTeamMate));
        given(userRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(inviter));

        //when
        teamMateCommandService.inviteTeamMate(dtoMapper.toInviteCommand(dto, 1L));

        //then
        assertThat(inviteeTeamMate.getStatus()).isEqualTo(TeamMateStatus.WAITING);
        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test @DisplayName("팀원 초대 수락")
    void applyInviteReply() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        User inviter = TestEntityFactory.user(2L, "inviter");
        TeamMate inviteeTm = goal.join(TestEntityFactory.user(2L, "invitee"));

        NotificationReceiver receiver = new NotificationReceiver(inviteeTm.getUserId());
        Notification inviteNotification = Notification.builder()
                .userId(1L)
                .type(NotificationType.INVITE_GOAL)
                .title("title")
                .content("content")
                .receivers(List.of(receiver))
                .build();

        TeamMateInviteReplyCommand command = TeamMateInviteReplyCommand.builder()
                .teamMateId(1L)
                .notificationId(1L)
                .accept(true)
                .build();

        given(teamMateRepository.findTeamMateWithGoal(any(Long.class))).willReturn(Optional.of(inviteeTm));
        given(notificationRepository.findNotificationReceiver(any(Long.class), any(Long.class))).willReturn(Optional.of(receiver));
        given(userRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(inviter));

        //when
        TeamMateInviteReplyResult response = teamMateCommandService.applyInviteReply(command);

        //then
        assertThat(response.getGoalId()).isNotNull();
        assertThat(inviteeTm.getStatus()).isEqualTo(TeamMateStatus.ONGOING);
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

        //when
        teamMateCommandService.inviteReject(new InviteReplyCommand(invitee.getId(), notification.getId()));

        //then
        assertThat(teamMate.getStatus()).isEqualTo(TeamMateStatus.REJECT);
        assertThat(receiver.isChecked()).isTrue();
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
