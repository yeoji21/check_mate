package checkmate.user.application;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.ErrorCode;
import checkmate.user.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFindServiceTest {
    @InjectMocks private UserFindService userFindService;
    @Mock private UserRepository userRepository;

    @Test
    void 닉네임_중복_통과_테스트() throws Exception{
        //given
        when(userRepository.findByNickname(any(String.class))).thenReturn(Optional.empty());
        //when
        userFindService.existsNicknameCheck("notExistsNickname");
        //then
        verify(userRepository).findByNickname(any(String.class));
    }

    @Test
    void 닉네임_중복_예외_테스트() throws Exception{
        when(userRepository.findByNickname(any(String.class))).thenReturn(Optional.of(TestEntityFactory.user(1L, "tester")));
        BusinessException exception = assertThrows(BusinessException.class, () -> userFindService.existsNicknameCheck("existsNickname"));
        verify(userRepository).findByNickname(any(String.class));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_NICKNAME);
    }
}