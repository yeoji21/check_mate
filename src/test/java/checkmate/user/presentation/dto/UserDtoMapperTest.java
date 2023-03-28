package checkmate.user.presentation.dto;

import checkmate.MapperTest;
import checkmate.user.application.dto.request.UserNicknameModifyCommand;
import checkmate.user.application.dto.request.UserSignUpCommand;
import checkmate.user.presentation.dto.request.UserNicknameModifyDto;
import checkmate.user.presentation.dto.request.UserSignUpDto;
import org.junit.jupiter.api.Test;

class UserDtoMapperTest extends MapperTest {
    private static final UserDtoMapper mapper = UserDtoMapper.INSTANCE;

    @Test
    void userSignUpDto() throws Exception {
        //given
        UserSignUpDto dto = UserSignUpDto.builder()
                .identifier("identifier")
                .username("username")
                .emailAddress("email@test.com")
                .nickname("nickname")
                .build();

        //when
        UserSignUpCommand command = mapper.toCommand(dto);

        //then
        isEqualTo(command.username(), dto.getUsername());
        isEqualTo(command.emailAddress(), dto.getEmailAddress());
        isEqualTo(command.nickname(), dto.getNickname());
        isEqualTo(command.identifier(), dto.getIdentifier());
    }

    @Test
    void userNicknameModifyCommand() throws Exception {
        //given
        UserNicknameModifyDto dto = new UserNicknameModifyDto("newNickname");
        long userId = 1L;

        //when
        UserNicknameModifyCommand command = mapper.toCommand(userId, dto);

        //then
        isEqualTo(command.userId(), userId);
        isEqualTo(command.nickname(), dto.getNickname());
    }
}