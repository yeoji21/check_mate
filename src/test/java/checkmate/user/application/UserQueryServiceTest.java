package checkmate.user.application;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.user.infrastructure.UserQueryDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {
    @Mock
    private UserQueryDao userQueryDao;
    @InjectMocks
    private UserQueryService userQueryService;

    @Test
    @DisplayName("닉네임 중복 확인")
    void existsNicknameCheck() throws Exception {
        //given
        when(userQueryDao.isExistsNickname(any(String.class))).thenReturn(false);

        //when
        userQueryService.existsNicknameCheck("nickname");

        //then
    }

    @Test
    @DisplayName("닉네임 중복 확인 - 이미 존재하는 닉네임")
    void existsNicknameCheck_duplicate() throws Exception {
        //given
        when(userQueryDao.isExistsNickname(any(String.class))).thenReturn(true);

        //when
        BusinessException exception = assertThrows(BusinessException.class, () -> userQueryService.existsNicknameCheck("nickname"));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_NICKNAME);
    }
}