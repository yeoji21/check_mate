package checkmate.user.application.dto;

import checkmate.MapperTest;
import checkmate.user.application.dto.request.UserSignUpCommand;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;

class UserCommandMapperTest extends MapperTest {
    private static final UserCommandMapper mapper = UserCommandMapper.INSTANCE;

    @Test
    void toEntity() throws Exception {
        //given
        UserSignUpCommand command = UserSignUpCommand.builder()
                .identifier("identifier")
                .username("username")
                .emailAddress("email@test.com")
                .nickname("nickname")
                .build();

        //when
        User user = mapper.toEntity(command);

        //then
        isEqualTo(user.getUsername(), command.username());
        isEqualTo(user.getEmailAddress(), command.emailAddress());
        isEqualTo(user.getNickname(), command.nickname());
        isEqualTo(user.getIdentifier(), command.identifier());
    }
}