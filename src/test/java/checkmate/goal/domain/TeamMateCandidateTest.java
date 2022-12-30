package checkmate.goal.domain;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TeamMateCandidateTest {

    @Test @DisplayName("TeamMateCandidate 객체 생성 테스트")
    void create() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        User user = TestEntityFactory.user(1L, "user");
        TeamMate teamMate = goal.join(user);

        //when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> new TeamMateCandidate(teamMate, 10));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXCEED_GOAL_LIMIT);
    }

    @Test
    void initiate() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        ReflectionTestUtils.setField(goal.getPeriod(), "startDate", LocalDate.now().minusDays(10));
        User user = TestEntityFactory.user(1L, "user");
        TeamMate teamMate = goal.join(user);

        //when
        TeamMateCandidate candidate = new TeamMateCandidate(teamMate, 9);
        candidate.initiate();

        //then
        assertThat(teamMate.getStatus()).isEqualTo(TeamMateStatus.ONGOING);
        assertThat(teamMate.calcProgressPercent()).isGreaterThan(0L);
    }
}