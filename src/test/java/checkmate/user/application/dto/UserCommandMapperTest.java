package checkmate.user.application.dto;

import checkmate.MapperTest;
import checkmate.user.application.dto.request.UserSignUpCommand;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;

class UserCommandMapperTest extends MapperTest {
    private static final UserCommandMapper mapper = UserCommandMapper.INSTANCE;

    @Test
    void toEntity() throws Exception{
        //given
        UserSignUpCommand command = UserSignUpCommand.builder()
                .providerId("providerId")
                .username("username")
                .emailAddress("email@test.com")
                .nickname("nickname")
                .fcmToken("fcmToken")
                .build();

        //when
        User user = mapper.toEntity(command);

        //then
        isEqualTo(user.getProviderId(), command.providerId());
        isEqualTo(user.getUsername(), command.username());
        isEqualTo(user.getEmailAddress(), command.emailAddress());
        isEqualTo(user.getNickname(), command.nickname());
        isEqualTo(user.getFcmToken(), command.fcmToken());
    }
}