package checkmate.user.application;

import checkmate.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {
    @InjectMocks
    private UserQueryService userQueryService;
    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("닉네임 중복 확인")
    void existsNicknameCheck() throws Exception {
        //given
        String nickname = "findNickname";
        when(userRepository.findByNickname(any(String.class))).thenReturn(Optional.empty());

        //when
        userQueryService.existsNicknameCheck(nickname);

        //then
        verify(userRepository).findByNickname(any(String.class));
    }
}