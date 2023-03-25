package checkmate.user.application;

import checkmate.TestEntityFactory;
import checkmate.user.application.dto.UserCommandMapper;
import checkmate.user.application.dto.request.SignUpCommand;
import checkmate.user.application.dto.request.UserNicknameModifyCommand;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserCommandMapper userCommandMapper;
    @InjectMocks
    private UserCommandService userRegisterService;

    @Test
    @DisplayName("회원 가입")
    void signUp() throws Exception {
        //given
        SignUpCommand command = createUserSignUpCommand();
        User user = createUser(command);
        given(userCommandMapper.toEntity(any(SignUpCommand.class))).willReturn(user);
        doAnswer((invocation) -> {
            User argument = (User) invocation.getArgument(0);
            ReflectionTestUtils.setField(argument, "id", 1L);
            return argument;
        }).when(userRepository).save(any(User.class));

        //when
        userRegisterService.signUp_v1(command);

        //then
        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("닉네임 변경")
    void nicknameUpdate() throws Exception {
        //given
        User user = createUser();
        UserNicknameModifyCommand command = new UserNicknameModifyCommand(1L, "newNickname");
        given(userRepository.findById(any(Long.class))).willReturn(Optional.of(user));

        //when
        userRegisterService.nicknameUpdate(command);

        //then
        assertThat(user.getNickname()).isEqualTo(command.nickname());
    }

    private User createUser() {
        return TestEntityFactory.user(1L, "user");
    }

    private User createUser(SignUpCommand command) {
        return User.builder()
                .providerId(command.providerId())
                .username(command.username())
                .nickname(command.nickname())
                .emailAddress(command.emailAddress())
                .build();
    }

    private SignUpCommand createUserSignUpCommand() {
        return SignUpCommand.builder()
                .providerId("providerId")
                .username("여지원")
                .nickname("yeoz1")
                .emailAddress("test@naver.com")
                .build();
    }
}