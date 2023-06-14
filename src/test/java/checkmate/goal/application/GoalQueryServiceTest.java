package checkmate.goal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import checkmate.TestEntityFactory;
import checkmate.goal.application.dto.response.GoalHistoryInfo;
import checkmate.goal.domain.Goal;
import checkmate.goal.infra.GoalQueryDao;
import checkmate.mate.application.dto.response.GoalHistoryInfoResult;
import checkmate.mate.domain.Mate;
import checkmate.mate.infra.MateQueryDao;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoalQueryServiceTest {

    @Mock
    private GoalQueryDao goalQueryDao;
    @Mock
    private MateQueryDao mateQueryDao;
    @InjectMocks
    private GoalQueryService goalQueryService;


    @Test
    void findGoalHistoryResult() throws Exception {
        //given
        long userId = 1L;
        given(goalQueryDao.findGoalHistoryInfo(userId)).willReturn(createGoalHistoryInfoList());
        given(mateQueryDao.findMateNicknames(anyList())).willReturn(
            Map.of(1L, List.of("nickname1", "nickname2")));

        //when
        GoalHistoryInfoResult result = goalQueryService.findGoalHistoryResult(userId);

        //then
        assertThat(result.goals()).hasSize(2);
        assertThat(result.goals()).allMatch(info -> info.getAchievementRate() >= 0.0);
        assertThat(result.goals()).allMatch(info -> info.getCheckDays() != null);
        assertThat(result.goals()).allMatch(info -> info.getMateNicknames().size() == 2);
    }

    private List<GoalHistoryInfo> createGoalHistoryInfoList() {
        Goal goal = TestEntityFactory.goal(1L, "title");
        Mate mate1 = goal.createMate(TestEntityFactory.user(1L, "nickname1"));
        Mate mate2 = goal.createMate(TestEntityFactory.user(2L, "nickname2"));
        return List.of(new GoalHistoryInfo(mate1), new GoalHistoryInfo(mate2));
    }
}