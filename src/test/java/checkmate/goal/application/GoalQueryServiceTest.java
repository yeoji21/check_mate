package checkmate.goal.application;

import checkmate.TestEntityFactory;
import checkmate.goal.application.dto.GoalQueryMapper;
import checkmate.goal.application.dto.response.GoalSimpleInfo;
import checkmate.goal.application.dto.response.TodayGoalInfo;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.infrastructure.GoalQueryDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalQueryServiceTest {
    @Mock private GoalRepository goalRepository;
    @Mock private GoalQueryDao goalQueryDao;
    private GoalQueryService goalQueryService;
    private GoalQueryMapper goalQueryMapper = GoalQueryMapper.INSTANCE;

    private TeamMate teamMate;
    private Goal goal;
    @BeforeEach
    void setUp() {
        goalQueryService = new GoalQueryService(goalQueryDao, goalRepository, goalQueryMapper);

        teamMate = TestEntityFactory.teamMate(1L, 1L);
        goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        goal.addTeamMate(teamMate);
    }

    @Test
    void 유저가_현재_진행중인_목표들_조회() throws Exception{
        //given
        Goal secondGoal = TestEntityFactory.goal(1L, "testGoal");
        when(goalRepository.findOngoingGoalList(any(Long.class))).thenReturn(List.of(goal, secondGoal));

        //when
        List<GoalSimpleInfo> response = goalQueryService.findOngoingSimpleInfo(1L);

        //then
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getTitle()).isEqualTo("자바의 정석 스터디");
    }

    @Test
    void 유저가_오늘해야할_목표_조회_테스트() throws Exception{
        //given

        //when
        List<TodayGoalInfo> result = goalQueryService.findTodayGoalInfo(1L);

        //then
        verify(goalQueryDao).findTodayGoalInfoDtoList(any(Long.class));
    }
}