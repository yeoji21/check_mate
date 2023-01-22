package checkmate.user.presentation.dto;

import checkmate.MapperTest;
import checkmate.user.application.dto.request.UserNicknameModifyCommand;
import checkmate.user.application.dto.request.UserSignUpCommand;
import checkmate.user.domain.ProviderIdGenerator;
import checkmate.user.presentation.dto.request.GoogleSignUpDto;
import checkmate.user.presentation.dto.request.KakaoSignUpDto;
import checkmate.user.presentation.dto.request.NaverSignUpDto;
import checkmate.user.presentation.dto.request.UserNicknameModifyDto;
import org.junit.jupiter.api.Test;

class UserDtoMapperTest extends MapperTest {
    private static final UserDtoMapper mapper = UserDtoMapper.INSTANCE;

    @Test
    void kakaoSignUpDto() throws Exception{
        //given
        KakaoSignUpDto dto = KakaoSignUpDto.builder()
                .providerId("providerId")
                .username("username")
                .emailAddress("emailAddress@test.com")
                .nickname("nickname")
                .fcmToken("fcmToken")
                .build();

        //when
        UserSignUpCommand command = mapper.toCommand(dto);

        //then
        isEqualTo(command.providerId(), ProviderIdGenerator.kakao(dto.getProviderId()));
        isEqualTo(command.username(), dto.getUsername());
        isEqualTo(command.emailAddress(), dto.getEmailAddress());
        isEqualTo(command.nickname(), dto.getNickname());
        isEqualTo(command.fcmToken(), dto.getFcmToken());
    }

    @Test
    void naverSignUpDto() throws Exception{
        //given
        NaverSignUpDto dto = NaverSignUpDto.builder()
                .providerId("providerId")
                .username("username")
                .emailAddress("emailAddress@test.com")
                .nickname("nickname")
                .fcmToken("fcmToken")
                .build();

        //when
        UserSignUpCommand command = mapper.toCommand(dto);

        //then
        isEqualTo(command.providerId(), ProviderIdGenerator.naver(dto.getProviderId()));
        isEqualTo(command.username(), dto.getUsername());
        isEqualTo(command.emailAddress(), dto.getEmailAddress());
        isEqualTo(command.nickname(), dto.getNickname());
        isEqualTo(command.fcmToken(), dto.getFcmToken());
    }

    @Test
    void googleSignUpDto() throws Exception{
        //given
        GoogleSignUpDto dto = GoogleSignUpDto.builder()
                .providerId("providerId")
                .username("username")
                .emailAddress("emailAddress@test.com")
                .nickname("nickname")
                .fcmToken("fcmToken")
                .build();

        //when
        UserSignUpCommand command = mapper.toCommand(dto);

        //then
        isEqualTo(command.providerId(), ProviderIdGenerator.google(dto.getProviderId()));
        isEqualTo(command.username(), dto.getUsername());
        isEqualTo(command.emailAddress(), dto.getEmailAddress());
        isEqualTo(command.nickname(), dto.getNickname());
        isEqualTo(command.fcmToken(), dto.getFcmToken());
    }

    @Test
    void userNicknameModifyCommand() throws Exception{
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