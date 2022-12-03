package checkmate.user.application;

import checkmate.TestEntityFactory;
import checkmate.exception.format.BusinessException;
import checkmate.exception.format.ErrorCode;
import checkmate.user.application.dto.UserCommandMapper;
import checkmate.user.application.dto.request.UserNicknameModifyCommand;
import checkmate.user.application.dto.request.UserSignUpCommand;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private UserCommandMapper userCommandMapper;
    @InjectMocks private UserCommandService userRegisterService;

    @Test
    void 회원가입_테스트() throws Exception{
        //given
        UserSignUpCommand command = UserSignUpCommand.builder()
                .providerId("providerId")
                .username("여지원")
                .nickname("yeoz1")
                .email("test@naver.com")
                .build();
        //when
        given(userCommandMapper.toEntity(any(UserSignUpCommand.class))).willReturn(TestEntityFactory.user(null, "user"));
        doAnswer((invocation) -> {
            User argument = (User) invocation.getArgument(0);
            ReflectionTestUtils.setField(argument, "id", 1L);
            return argument;
        }).when(userRepository).save(any(User.class));

        userRegisterService.signUp(command);

        //then
    }

    @Test
    void 닉네임_변경_테스트() throws Exception{
        User user = TestEntityFactory.user(1L, "tester");

        UserNicknameModifyCommand command = new UserNicknameModifyCommand(1L, "newNickname");
        given(userRepository.findById(any(Long.class))).willReturn(Optional.of(user));

        userRegisterService.nicknameUpdate(command);

        assertThat(user.getNickname()).isEqualTo(command.getNickname());
    }

    @Test
    void 닉네임_변경_기간_실패_테스트() throws Exception{
        User user = TestEntityFactory.user(1L, "tester");
        user.changeNickname("change");
        UserNicknameModifyCommand command = new UserNicknameModifyCommand(1L, "newNickname");
        given(userRepository.findById(any(Long.class))).willReturn(Optional.of(user));

        BusinessException exception = assertThrows(BusinessException.class, () -> userRegisterService.nicknameUpdate(command));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UPDATE_DURATION);
    }
}