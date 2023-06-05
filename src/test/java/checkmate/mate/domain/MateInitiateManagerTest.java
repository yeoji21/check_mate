package checkmate.mate.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate.MateStatus;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import checkmate.user.infrastructure.UserQueryDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MateInitiateManagerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserQueryDao userQueryDao;
    @InjectMocks
    private MateInitiateManager mateInitiateManager;

    @Test
    @DisplayName("목표 시작 성공")
    void initiate_success() throws Exception {
        //given
        Mate mate = createMate();
        mate.toWaitingStatus();
        given(userQueryDao.countOngoingGoals(any(Long.class))).willReturn(1);

        //when
        MateStatus before = mate.getStatus();
        mateInitiateManager.initiate(mate);

        //then
        MateStatus after = mate.getStatus();
        assertThat(before).isEqualTo(MateStatus.WAITING);
        assertThat(after).isEqualTo(MateStatus.ONGOING);
    }

    @Test
    @DisplayName("목표 시작 실패 - 진행 중인 목표 개수 초과")
    void initiate_ongoing_count_fail() throws Exception {
        //given
        Mate mate = createMate();
        given(userQueryDao.countOngoingGoals(any(Long.class))).willReturn(10);

        //when then
        BusinessException exception = assertThrows(BusinessException.class,
            () -> mateInitiateManager.initiate(mate));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXCEED_GOAL_LIMIT);
    }

    @Test
    @DisplayName("목표 시작 실패 - 잘못된 팀원의 상태")
    void initiate_team_mate_status_fail() throws Exception {
        //given
        Mate mate = createMate();
        ReflectionTestUtils.setField(mate, "status", MateStatus.ONGOING);
        given(userQueryDao.countOngoingGoals(any(Long.class))).willReturn(1);

        //when then
        BusinessException exception = assertThrows(BusinessException.class,
            () -> mateInitiateManager.initiate(mate));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MATE_STATUS);
    }

    private Mate createMate() {
        Goal goal = TestEntityFactory.goal(1L, "title");
        User user = TestEntityFactory.user(2L, "user");
        return goal.join(user);
    }
}