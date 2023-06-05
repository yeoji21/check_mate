package checkmate.mate.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import checkmate.exception.BusinessException;
import checkmate.exception.UnInviteableGoalException;
import checkmate.exception.code.ErrorCode;
import checkmate.mate.domain.Mate.MateStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MateStatusTest {

    @Test
    @DisplayName("초대 가능 여부 검사 - 이미 목표에 참여 중")
    void inviteableCheck_already_in_goal() throws Exception {
        //given

        //when
        UnInviteableGoalException exception = assertThrows(UnInviteableGoalException.class,
            () -> MateStatus.ONGOING.inviteableCheck());
        assertThrows(UnInviteableGoalException.class,
            () -> MateStatus.SUCCESS.inviteableCheck());

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ALREADY_IN_GOAL);
    }

    @Test
    @DisplayName("초대 가능 여부 검사 - 이미 초대 요청을 보냄")
    void inviteableCheck_duplicate_invite() throws Exception {
        //given

        //when
        UnInviteableGoalException exception = assertThrows(UnInviteableGoalException.class,
            () -> MateStatus.WAITING.inviteableCheck());

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_INVITE_REQUEST);
    }

    @Test
    @DisplayName("초대 가능 여부 검사 - 성공")
    void inviteableCheck_success() throws Exception {
        //given

        //when
        MateStatus.CREATED.inviteableCheck();
        MateStatus.REJECT.inviteableCheck();
        MateStatus.OUT.inviteableCheck();

        //then
    }

    @Test
    @DisplayName("목표 시작 가능 여부 검사 - 실패")
    void initiateableCheck_fail() throws Exception {
        //given

        //when
        assertThrows(BusinessException.class,
            () -> MateStatus.CREATED.initiateableCheck());
        assertThrows(BusinessException.class,
            () -> MateStatus.ONGOING.initiateableCheck());
        assertThrows(BusinessException.class,
            () -> MateStatus.REJECT.initiateableCheck());
        assertThrows(BusinessException.class,
            () -> MateStatus.OUT.initiateableCheck());
        assertThrows(BusinessException.class,
            () -> MateStatus.SUCCESS.initiateableCheck());

        //then
    }

    @Test
    @DisplayName("목표 시작 가능 여부 검사 - 성공")
    void initiateableCheck_success() throws Exception {
        //given

        //when
        MateStatus.WAITING.initiateableCheck();

        //then

    }
}