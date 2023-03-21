package checkmate.user.domain;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {
    @Test
    @DisplayName("닉네임 변경")
    void changeNickname() throws Exception {
        //given
        User user = TestEntityFactory.user(1L, "user");
        String newNickname = "newNickname";

        //when
        user.changeNickname(newNickname);

        //then
        assertThat(user.getNickname()).isEqualTo(newNickname);
        assertThat(user.getNicknameUpdatedDate()).isNotNull();
    }

    @Test
    @DisplayName("닉네임 변경 실패")
    void changeNickname_fail() throws Exception {
        //given
        User user = TestEntityFactory.user(1L, "user");
        ReflectionTestUtils.setField(user, "nicknameUpdatedDate", LocalDate.now());
        String newNickname = "newNickname";

        //when
        BusinessException exception = assertThrows(BusinessException.class, () -> user.changeNickname(newNickname));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UPDATE_DURATION);
    }
}