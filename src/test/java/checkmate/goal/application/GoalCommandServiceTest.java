package checkmate.goal.application;

import checkmate.TestEntityFactory;
import checkmate.common.cache.CacheTemplate;
import checkmate.exception.ExceedGoalLimitException;
import checkmate.goal.application.dto.GoalCommandMapper;
import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalCategory;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.presentation.dto.GoalDtoMapper;
import checkmate.goal.presentation.dto.request.GoalCreateDto;
import checkmate.goal.presentation.dto.request.GoalModifyDto;
import checkmate.notification.domain.event.StaticNotificationCreatedEvent;
import checkmate.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class GoalCommandServiceTest {
    @Mock private GoalRepository goalRepository;
    @Mock private CacheTemplate cacheTemplate;
    @Mock private ApplicationEventPublisher eventPublisher;
    private GoalCommandMapper commandMapper = GoalCommandMapper.INSTANCE;
    private GoalDtoMapper dtoMapper = GoalDtoMapper.INSTANCE;
    private GoalCommandService goalCommandService;

    private TeamMate teamMate;
    private Goal goal;
    private User user;
    @BeforeEach
    void setUp() {
        goalCommandService = new GoalCommandService(goalRepository, eventPublisher, cacheTemplate, commandMapper);

        user = TestEntityFactory.user(1L, "tester");
        teamMate = TestEntityFactory.teamMate(1L, user.getId());
        goal = TestEntityFactory.goal(1L, "testGoal");
        goal.addTeamMate(teamMate);
    }

    @Test
    void 성공한_목표_처리_스케쥴러_테스트() throws Exception{
        Goal goal1 = TestEntityFactory.goal(1L, "testGoal1");
        Goal goal2 = TestEntityFactory.goal(3L, "testGoal3");

        TeamMate teamMate1 = TestEntityFactory.teamMate(1L, 1L);
        TeamMate teamMate2 = TestEntityFactory.teamMate(2L, 2L);
        TeamMate teamMate3 = TestEntityFactory.teamMate(3L, 3L);

        teamMate1.changeToOngoingStatus(0);
        teamMate2.changeToOngoingStatus(0);
        teamMate3.changeToOngoingStatus(0);

        goal1.addTeamMate(teamMate1);
        goal1.addTeamMate(teamMate2);
        goal2.addTeamMate(teamMate3);

        //given
        given(goalRepository.updateYesterdayOveredGoals()).willReturn(List.of(goal1, goal2));

        //when
        goalCommandService.updateYesterdayOveredGoals();

        //then
        verify(eventPublisher).publishEvent(any(StaticNotificationCreatedEvent.class));
        verify(cacheTemplate).deleteTMCacheData(any(List.class));
    }

    @Test
    void 목표수정_테스트() throws Exception{
        //given
        LocalDate endDate = goal.getEndDate();
        LocalTime now = LocalTime.now();
        GoalModifyDto dto = GoalModifyDto.builder()
                .endDate(endDate.plusDays(10L))
                .appointmentTime(now)
                .build();

        given(goalRepository.checkUserIsInGoal(any(Long.class), any(Long.class))).willReturn(true);
        given(goalRepository.findByIdForUpdate(any(Long.class))).willReturn(Optional.of(goal));

        //when
        goalCommandService.modifyGoal(dtoMapper.toModifyCommand(dto, 1L, 1L));

        //then
        assertThat(goal.getEndDate()).isAfter(endDate);
        assertThat(goal.getAppointmentTime()).isEqualTo(now);
    }

    @Test
    void 목표저장_테스트(){
        //given
        GoalCreateCommand command = GoalCreateCommand.builder()
                .userId(1L)
                .category(GoalCategory.LEARNING)
                .title("testGoal")
                .startDate(LocalDate.now().minusDays(10L))
                .endDate(LocalDate.now().plusDays(30L))
                .checkDays("월수금")
                .build();
        given(goalRepository.countOngoingGoals(any(Long.class))).willReturn(0);
        doAnswer((invocation) -> {
            Goal argument = (Goal) invocation.getArgument(0);
            ReflectionTestUtils.setField(argument, "id", 1L);
            return argument;
        }).when(goalRepository).save(any(Goal.class));

        //when
        long goalId = goalCommandService.create(command);

        //then
        assertThat(goalId).isGreaterThan(0L);
    }

    @Test
    void 목표생성한_유저의_현재목표가_최대치_이상() throws Exception{
        GoalCreateDto goalCreateDto = GoalCreateDto.builder()
                .category(GoalCategory.LEARNING)
                .title("testGoal")
                .startDate(LocalDate.now().minusDays(10L))
                .endDate(LocalDate.now().plusDays(30L))
                .checkDays("월수금")
                .build();
        given(goalRepository.countOngoingGoals(any(Long.class))).willReturn(11);
        assertThrows(ExceedGoalLimitException.class,
                () -> goalCommandService.create(dtoMapper.toCreateCommand(goalCreateDto, 1L)));
    }
}
