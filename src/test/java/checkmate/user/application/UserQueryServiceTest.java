package checkmate.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.user.infrastructure.UserQueryDao;
import com.navercorp.fixturemonkey.FixtureMonkey;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    private UserQueryDao userQueryDao;
    @InjectMocks
    private UserQueryService sut;

    @Test
    @DisplayName("닉네임 중복 확인")
    void existsNicknameCheck() throws Exception {
        //given
        when(userQueryDao.isExistsNickname(any(String.class))).thenReturn(false);

        //when
        sut.existsNicknameCheck("nickname");

        //then
    }

    @Test
    @DisplayName("닉네임 중복 확인 - 이미 존재하는 닉네임")
    void existsNicknameCheck_duplicate() throws Exception {
        //given
        when(userQueryDao.isExistsNickname(any(String.class))).thenReturn(true);

        //when
        BusinessException exception = assertThrows(BusinessException.class,
            () -> sut.existsNicknameCheck("nickname"));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_NICKNAME);
    }

    @Test
    void find_user_weekly_schdule() throws Exception {
        //given
        LocalDate requestDate = FixtureMonkey.create().giveMeOne(LocalDate.class);
        List<LocalDate> datesOfWeek = new ArrayList<>();
        doAnswer((invocation -> {
            List<LocalDate> argument = invocation.getArgument(1);
            datesOfWeek.addAll(argument);
            return null;
        })).when(userQueryDao).findSchedule(anyLong(), anyList());

        //when
        sut.getWeeklySchdule(1L, requestDate);

        //then
        assertThat(datesOfWeek).allMatch(date -> isSameWeek(requestDate, date));
    }

    private boolean isSameWeek(LocalDate requestDate, LocalDate date) {
        return date.get(ChronoField.ALIGNED_WEEK_OF_YEAR) ==
            requestDate.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
    }
}