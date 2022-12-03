package checkmate.goal.application;

import checkmate.TestEntityFactory;
import checkmate.common.cache.CacheTemplate;
import checkmate.exception.format.BusinessException;
import checkmate.exception.format.ErrorCode;
import checkmate.goal.application.dto.GoalCommandMapper;
import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.domain.*;
import checkmate.goal.domain.event.GoalCreatedEvent;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.user.domain.User;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class GoalCommandServiceTest {
    @Mock private GoalRepository goalRepository;
    @Mock private CacheTemplate cacheTemplate;
    @Mock private TeamMateRepository teamMateRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Spy
    private GoalCommandMapper commandMapper = GoalCommandMapper.INSTANCE;
    @InjectMocks
    private GoalCommandService goalCommandService;

    @Test
    void 성공한_목표_처리_스케쥴러_테스트() throws Exception{
        Goal goal1 = TestEntityFactory.goal(1L, "testGoal1");
        Goal goal2 = TestEntityFactory.goal(3L, "testGoal3");

        User user1 = TestEntityFactory.user(1L, "user1");
        User user2 = TestEntityFactory.user(2L, "user2");
        User user3 = TestEntityFactory.user(3L, "user3");

        TeamMate teamMate1 = goal1.join(user1);
        TeamMate teamMate2 = goal1.join(user2);
        TeamMate teamMate3 = goal2.join(user3);

        ReflectionTestUtils.setField(teamMate1, "status", TeamMateStatus.ONGOING);
        ReflectionTestUtils.setField(teamMate2, "status", TeamMateStatus.ONGOING);
        ReflectionTestUtils.setField(teamMate3, "status", TeamMateStatus.ONGOING);

        //given
        given(goalRepository.updateYesterdayOveredGoals()).willReturn(List.of(goal1.getId(), goal2.getId()));
        given(teamMateRepository.findTeamMates(anyList())).willReturn(List.of(teamMate1, teamMate2, teamMate3));

        //when
        goalCommandService.updateYesterdayOveredGoals();

        //then
        verify(eventPublisher).publishEvent(any(NotPushNotificationCreatedEvent.class));
        verify(cacheTemplate).deleteTMCacheData(any(List.class));
    }

    @Test
    void 목표수정_테스트() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "testGoal");
        LocalDate endDate = goal.getEndDate();
        LocalTime now = LocalTime.now();
        GoalModifyCommand command = GoalModifyCommand.builder()
                .endDate(endDate.plusDays(10L))
                .appointmentTime(now)
                .build();

        given(goalRepository.checkUserIsInGoal(any(Long.class), any(Long.class))).willReturn(true);
        given(goalRepository.findByIdForUpdate(any(Long.class))).willReturn(Optional.of(goal));

        //when
        goalCommandService.modifyGoal(command);

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
        verify(eventPublisher).publishEvent(any(GoalCreatedEvent.class));
    }

    @Test
    void 목표생성한_유저의_현재목표가_최대치_이상() throws Exception{
        GoalCreateCommand command = GoalCreateCommand.builder()
                .userId(1L)
                .category(GoalCategory.LEARNING)
                .title("testGoal")
                .startDate(LocalDate.now().minusDays(10L))
                .endDate(LocalDate.now().plusDays(30L))
                .checkDays("월수금")
                .build();
        given(goalRepository.countOngoingGoals(any(Long.class))).willReturn(11);
        BusinessException exception = assertThrows(BusinessException.class, () -> goalCommandService.create(command));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXCEED_GOAL_LIMIT);
    }
}
