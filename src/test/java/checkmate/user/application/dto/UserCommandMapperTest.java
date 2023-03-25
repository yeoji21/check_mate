package checkmate.user.application.dto;

import checkmate.MapperTest;
import checkmate.user.application.dto.request.SignUpCommand;
import checkmate.user.application.dto.request.UserSignUpCommand;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;

class UserCommandMapperTest extends MapperTest {
    private static final UserCommandMapper mapper = UserCommandMapper.INSTANCE;

    @Test
    void toEntity_v1() throws Exception {
        //given
        SignUpCommand command = SignUpCommand.builder()
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

    @Test
    void toEntity() throws Exception {
        //given
        UserSignUpCommand command = UserSignUpCommand.builder()
                .userIdentifier("userIdentifier")
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
        isEqualTo(user.getUserIdentifier(), command.userIdentifier());
    }
}