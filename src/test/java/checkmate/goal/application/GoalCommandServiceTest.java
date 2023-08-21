package checkmate.goal.application;

import static org.assertj.core.api.Assertions.assertThat;

import checkmate.TestEntityFactory;
import checkmate.goal.application.dto.GoalCommandMapper;
import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.Goal.GoalCategory;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.infra.FakeGoalRepository;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.infra.FakeMateRepository;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import checkmate.user.infrastructure.FakeUserRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoalCommandServiceTest {

    @Spy
    private GoalRepository goalRepository = new FakeGoalRepository();
    @Spy
    private UserRepository userRepository = new FakeUserRepository();
    @Spy
    private MateRepository mateRepository = new FakeMateRepository();
    @Spy
    private GoalCommandMapper commandMapper = GoalCommandMapper.INSTANCE;
    @InjectMocks
    private GoalCommandService goalCommandService;

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
    }

    private GoalCreateCommand createGoalCreateCommand() {
        User user = createAndSaveUser();
        return GoalCreateCommand.builder()
            .userId(user.getId())
            .category(GoalCategory.LEARNING)
            .title("goal")
            .startDate(LocalDate.now().minusDays(10L))
            .endDate(LocalDate.now().plusDays(30L))
            .checkDays(new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY})
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
