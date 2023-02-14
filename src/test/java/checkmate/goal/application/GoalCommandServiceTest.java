package checkmate.goal.application;

import checkmate.TestEntityFactory;
import checkmate.common.cache.CacheTemplate;
import checkmate.goal.application.dto.GoalCommandMapper;
import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.domain.*;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class GoalCommandServiceTest {
    @Mock
    private GoalRepository goalRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TeamMateRepository teamMateRepository;
    @Mock
    private TeamMateInitiateManager teamMateInitiateManager;
    @Mock
    private CacheTemplate cacheTemplate;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Spy
    private GoalCommandMapper commandMapper = GoalCommandMapper.INSTANCE;
    @InjectMocks
    private GoalCommandService goalCommandService;

    @Test
    void 성공한_목표_처리_스케쥴러_테스트() throws Exception {
        //given
        Goal goal1 = TestEntityFactory.goal(1L, "testGoal1");
        Goal goal2 = TestEntityFactory.goal(3L, "testGoal3");

        TeamMate teamMate1 = getTeamMate(goal1, 1L);
        TeamMate teamMate2 = getTeamMate(goal1, 2L);
        TeamMate teamMate3 = getTeamMate(goal2, 3L);

        given(goalRepository.updateYesterdayOveredGoals()).willReturn(List.of(goal1.getId(), goal2.getId()));
        given(teamMateRepository.findTeamMates(anyList())).willReturn(List.of(teamMate1, teamMate2, teamMate3));

        //when
        goalCommandService.updateYesterdayOveredGoals();

        //then
        verify(eventPublisher).publishEvent(any(NotPushNotificationCreatedEvent.class));
        verify(cacheTemplate).deleteTMCacheData(any(List.class));
    }

    @Test
    void 목표수정_테스트() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "testGoal");
        LocalDate beforeEndDate = goal.getEndDate();
        LocalTime beforeTime = goal.getAppointmentTime();
        GoalModifyCommand command = getGoalModifyCommand(goal);

        given(goalRepository.checkUserIsInGoal(any(Long.class), any(Long.class))).willReturn(true);
        given(goalRepository.findByIdForUpdate(any(Long.class))).willReturn(Optional.of(goal));

        //when
        goalCommandService.modifyGoal(command);

        //then
        assertThat(command.endDate()).isNotEqualTo(beforeEndDate);
        assertThat(command.appointmentTime()).isNotEqualTo(beforeTime);
        assertThat(goal.getEndDate()).isEqualTo(command.endDate());
        assertThat(goal.getAppointmentTime()).isEqualTo(command.appointmentTime());
    }

    @Test
    void 목표저장_테스트() {
        //given
        GoalCreateCommand command = getGoalCreateCommand();

        doAnswer((invocation) -> {
            Goal argument = (Goal) invocation.getArgument(0);
            ReflectionTestUtils.setField(argument, "id", 1L);
            return argument;
        }).when(goalRepository).save(any(Goal.class));

        given(userRepository.findById(any(Long.class)))
                .willReturn(Optional.ofNullable(TestEntityFactory.user(1L, "user")));

        //when
        long goalId = goalCommandService.create(command);

        //then
        assertThat(goalId).isGreaterThan(0L);
        verify(teamMateRepository).save(any(TeamMate.class));
        verify(teamMateInitiateManager).initiate(any(TeamMate.class));
    }

    private GoalCreateCommand getGoalCreateCommand() {
        return GoalCreateCommand.builder()
                .userId(1L)
                .category(GoalCategory.LEARNING)
                .title("testGoal")
                .startDate(LocalDate.now().minusDays(10L))
                .endDate(LocalDate.now().plusDays(30L))
                .checkDays("월수금")
                .build();
    }

    private TeamMate getTeamMate(Goal goal, long userId) {
        User user = TestEntityFactory.user(userId, "user1");
        TeamMate teamMate1 = goal.join(user);
        ReflectionTestUtils.setField(teamMate1, "status", TeamMateStatus.ONGOING);
        return teamMate1;
    }

    private GoalModifyCommand getGoalModifyCommand(Goal goal) {
        return GoalModifyCommand.builder()
                .endDate(goal.getEndDate().plusDays(10L))
                .appointmentTime(LocalTime.now())
                .build();
    }
}
