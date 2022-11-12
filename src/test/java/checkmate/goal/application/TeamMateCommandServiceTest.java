package checkmate.goal.application;

import checkmate.TestEntityFactory;
import checkmate.common.cache.CacheTemplate;
import checkmate.goal.application.dto.TeamMateCommandMapper;
import checkmate.goal.application.dto.request.TeamMateInviteReplyCommand;
import checkmate.goal.application.dto.response.TeamMateInviteReplyResult;
import checkmate.goal.domain.*;
import checkmate.goal.domain.service.TeamMateInviteService;
import checkmate.goal.presentation.dto.TeamMateDtoMapper;
import checkmate.goal.presentation.dto.request.TeamMateInviteDto;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.notification.domain.event.StaticNotificationCreatedEvent;
import checkmate.notification.domain.factory.InviteGoalNotificationFactory;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeamMateCommandServiceTest {
    @Mock private GoalRepository goalRepository;
    @Mock private UserRepository userRepository;
    @Mock private TeamMateRepository teamMateRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private TeamMateInviteService teamMateInviteService;
    @Mock private CacheTemplate cacheTemplate;
    @Mock private ApplicationEventPublisher eventPublisher;
    TeamMateCommandMapper commandMapper = TeamMateCommandMapper.INSTANCE;
    TeamMateDtoMapper dtoMapper = TeamMateDtoMapper.INSTANCE;

    private TeamMateCommandService teamMateCommandService;

    private TeamMate teamMate;
    private Goal goal;

    @BeforeEach
    void setUp() {
        teamMateCommandService = new TeamMateCommandService(userRepository, goalRepository, teamMateRepository, notificationRepository,
                teamMateInviteService, cacheTemplate, eventPublisher, commandMapper);
        teamMate = TestEntityFactory.teamMate(1L, 1L);
        goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        goal.addTeamMate(teamMate);
    }

    @Test @DisplayName("목표 생성자 팀원 생성 후 목표 수행 시작")
    void initiatingGoalCreator() throws Exception{
        //given
        User user = TestEntityFactory.user(1L, "user");
        given(goalRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(goal));
        given(userRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(user));
        given(goalRepository.countOngoingGoals(any(Long.class))).willReturn(5);

        //when
        teamMateCommandService.initiatingGoalCreator(1L, 1L);

        //then
        verify(teamMateRepository).save(any(TeamMate.class));
    }

    @Test @DisplayName("초대를 거절한 적이 있는 유저에게 초대")
    void 팀원_초대_테스트() throws Exception{
        //given
        User inviter = TestEntityFactory.user(1L, "inviter");
        User invitee = TestEntityFactory.user(5L, "invitee");
        TeamMateInviteDto dto = new TeamMateInviteDto(1L, invitee.getNickname());

        TeamMate inviteeTeamMate = TestEntityFactory.teamMate(1L, invitee.getId());
        goal.addTeamMate(inviteeTeamMate);
        ReflectionTestUtils.setField(inviteeTeamMate, "status", TeamMateStatus.REJECT);

        given(goalRepository.findById(any(Long.class))).willReturn(Optional.of(goal));
        given(userRepository.findByNickname(any(String.class))).willReturn(Optional.of(invitee));
        given(teamMateRepository.findTeamMate(any(Long.class), any(Long.class))).willReturn(Optional.of(inviteeTeamMate));
        doAnswer(invocation -> {
            ReflectionTestUtils.setField(inviteeTeamMate, "status", TeamMateStatus.WAITING);
            return inviteeTeamMate;
        }).when(teamMateInviteService).invite(any(Goal.class), any(Optional.class), any(User.class));
        given(userRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(inviter));

        //when
        teamMateCommandService.inviteTeamMate(dtoMapper.toInviteCommand(dto, 1L));

        //then
        assertThat(inviteeTeamMate.getStatus()).isEqualTo(TeamMateStatus.WAITING);
        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    void 팀원_초대_수락_응답_테스트() throws Exception{
        //given
        User inviter = TestEntityFactory.user(2L, "inviter");
        TeamMate teamMate = TestEntityFactory.teamMate(2L, 1L);
        goal.addTeamMate(teamMate);
        Notification inviteNotification = new InviteGoalNotificationFactory()
                .generate(commandMapper.toInviteGoalNotificationDto(inviter, teamMate));

        inviteNotification.setUpReceivers(List.of(new NotificationReceiver(1L)));

        TeamMateInviteReplyCommand command = TeamMateInviteReplyCommand.builder()
                .teamMateId(1L)
                .notificationId(1L)
                .accept(true)
                .build();

        given(teamMateRepository.findTeamMate(any(Long.class))).willReturn(Optional.of(this.teamMate));
        given(notificationRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(inviteNotification));
        given(userRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(inviter));

        //when
        TeamMateInviteReplyResult response = teamMateCommandService.applyInviteReply(command);

        //then
        assertThat(response.getGoalId()).isNotNull();
        assertThat(teamMate.getStatus()).isEqualTo(TeamMateStatus.ONGOING);
    }

    @Test
    void 인증하지_않은_팀원_업데이트_테스트() throws Exception{
        //given
        List<TeamMate> hookeyTms = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            hookeyTms.add(TestEntityFactory.teamMate((long) i, i));
        }

        given(teamMateRepository.updateYesterdayHookyTMs()).willReturn(hookeyTms);
        given(teamMateRepository.eliminateOveredTMs(hookeyTms)).willReturn(Collections.EMPTY_LIST);

        //when
        teamMateCommandService.updateHookyTeamMate();

        //then
        verify(cacheTemplate).deleteTMCacheData(any(List.class));
        verify(eventPublisher).publishEvent(any(StaticNotificationCreatedEvent.class));
    }
}
