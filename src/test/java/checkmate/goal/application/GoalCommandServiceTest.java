package checkmate.goal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import checkmate.TestEntityFactory;
import checkmate.common.cache.CacheHandler;
import checkmate.goal.application.dto.GoalCommandMapper;
import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.Goal.GoalCategory;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.infra.FakeGoalRepository;
import checkmate.goal.infra.GoalQueryDao;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.domain.MateStartingService;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import checkmate.user.infrastructure.FakeUserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

// TODO: 2023/06/30 repository layer test double fake로 변경
@ExtendWith(MockitoExtension.class)
class GoalCommandServiceTest {

    @Spy
    private GoalRepository goalRepository = new FakeGoalRepository();
    @Spy
    private UserRepository userRepository = new FakeUserRepository();
    @Mock
    private GoalQueryDao goalQueryDao;
    @Mock
    private MateRepository mateRepository;
    @Mock
    private MateStartingService mateStartingService;
    @Mock
    private CacheHandler cacheHandler;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Spy
    private GoalCommandMapper commandMapper = GoalCommandMapper.INSTANCE;
    @InjectMocks
    private GoalCommandService goalCommandService;

    @Test
    @DisplayName("성공한 목표 처리 스케쥴러")
    void updateYesterdayOveredGoals() throws Exception {
        //given
        Goal goal1 = createGoal();
        Goal goal2 = createGoal();
        given(goalQueryDao.findYesterdayOveredGoals()).willReturn(
            List.of(goal1.getId(), goal2.getId()));

        //when
        goalCommandService.updateYesterdayOveredGoals();

        //then
        verify(eventPublisher).publishEvent(any(NotPushNotificationCreatedEvent.class));
        verify(cacheHandler).deleteUserCaches(any(List.class));
    }

    @Test
    @DisplayName("목표 수정")
    void modifyGoal() throws Exception {
        //given
        Goal goal = createGoal();
        GoalModifyCommand command = createGoalModifyCommand(goal);

        //when
        goalCommandService.modify(command);

        //then
        assertThat(goal.getEndDate()).isEqualTo(command.endDate());
        assertThat(goal.getAppointmentTime()).isEqualTo(command.appointmentTime());
    }

    @Test
    @DisplayName("새 목표 생성")
    void create() {
        //given
        GoalCreateCommand command = createGoalCreateCommand();

        //when
        long goalId = goalCommandService.create(command);

        //then
        assertThat(goalId).isGreaterThan(0L);
        verify(mateRepository).save(any(Mate.class));
        verify(mateStartingService).startToGoal(any(Mate.class));
    }

    private GoalCreateCommand createGoalCreateCommand() {
        User user = createAndSaveUser();
        return GoalCreateCommand.builder()
            .userId(user.getId())
            .category(GoalCategory.LEARNING)
            .title("goal")
            .startDate(LocalDate.now().minusDays(10L))
            .endDate(LocalDate.now().plusDays(30L))
            .checkDays("월수금")
            .build();
    }

    private GoalModifyCommand createGoalModifyCommand(Goal goal) {
        return GoalModifyCommand.builder()
            .goalId(goal.getId())
            .endDate(goal.getEndDate().plusDays(10L))
            .appointmentTime(LocalTime.now())
            .build();
    }

    private Goal createGoal() {
        return goalRepository.save(TestEntityFactory.goal(0L, "goal"));
    }

    private User createAndSaveUser() {
        return userRepository.save(TestEntityFactory.user(0L, "nickname"));
    }
}
