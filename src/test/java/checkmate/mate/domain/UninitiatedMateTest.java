package checkmate.mate.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate.MateStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UninitiatedMateTest {

    @Test
    @DisplayName("목표 시작 성공")
    void startToGoal_success() throws Exception {
        //given
        Mate mate = createMate();
        UninitiatedMate sut = createUninitiateMate(mate, 0);

        //when
        sut.initiate();

        //then
        assertThat(mate.getStatus()).isEqualTo(MateStatus.ONGOING);
    }

    @Test
    @DisplayName("목표 시작 실패 - 진행 중인 목표 개수 초과")
    void startToGoal_fail() throws Exception {
        //given
        Mate mate = createMate();
        UninitiatedMate sut = createUninitiateMate(mate, 10);

        //when //then
        BusinessException exception = assertThrows(BusinessException.class, sut::initiate);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXCEED_GOAL_COUNT_LIMIT);
    }

    private UninitiatedMate createUninitiateMate(Mate mate, int ongoingGoalCount) {
        return new UninitiatedMate(mate, ongoingGoalCount);
    }

    private Mate createMate() {
        Goal goal = TestEntityFactory.goal(1L, "test");
        Mate mate = goal.createMate(TestEntityFactory.user(1L, "test"));
        mate.receiveInvite();
        return mate;
    }
}