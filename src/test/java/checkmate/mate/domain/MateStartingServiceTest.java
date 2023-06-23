package checkmate.mate.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.user.infrastructure.UserQueryDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MateStartingServiceTest {

    @Mock
    private UserQueryDao userQueryDao;
    @InjectMocks
    private MateStartingService mateStartingService;

    @Test
    @DisplayName("목표 시작 성공")
    void startToGoal_success() throws Exception {
        //given
        Mate mate = Mockito.mock(Mate.class);
        given(userQueryDao.countOngoingGoals(any(Long.class))).willReturn(1);

        //when
        mateStartingService.startToGoal(mate);

        //then
        verify(mate).acceptInvite();
    }

    @Test
    @DisplayName("목표 시작 실패 - 진행 중인 목표 개수 초과")
    void startToGoal_fail() throws Exception {
        //given
        Mate mate = Mockito.mock(Mate.class);
        given(userQueryDao.countOngoingGoals(any(Long.class))).willReturn(10);

        //when then
        BusinessException exception = assertThrows(BusinessException.class,
            () -> mateStartingService.startToGoal(mate));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXCEED_GOAL_LIMIT);
    }
}