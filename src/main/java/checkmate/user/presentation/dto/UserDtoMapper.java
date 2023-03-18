package checkmate.user.presentation.dto;

import checkmate.user.application.dto.request.UserNicknameModifyCommand;
import checkmate.user.application.dto.request.UserSignUpCommand;
import checkmate.user.domain.ProviderIdGenerator;
import checkmate.user.presentation.dto.request.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {
    UserDtoMapper INSTANCE = Mappers.getMapper(UserDtoMapper.class);

    @Mapping(target = "providerId", source = "providerId", qualifiedByName = "kakaoId")
    UserSignUpCommand toCommand(KakaoSignUpDto kakaoSignUpDto);

    @Named("kakaoId")
    default String kakaoId(String providerId) {
        return ProviderIdGenerator.kakao(providerId);
    }

    @Mapping(target = "providerId", source = "providerId", qualifiedByName = "googleId")
    UserSignUpCommand toCommand(GoogleSignUpDto googleSignUpDto);

    @Named("googleId")
    default String googleId(String providerId) {
        return ProviderIdGenerator.google(providerId);
    }

    @Mapping(target = "providerId", source = "providerId", qualifiedByName = "naverId")
    UserSignUpCommand toCommand(NaverSignUpDto naverSignUpDto);

    @Named("naverId")
    default String naverId(String providerId) {
        return ProviderIdGenerator.naver(providerId);
    }

    @Mapping(source = "dto.nickname", target = "nickname")
    UserNicknameModifyCommand toCommand(long userId, UserNicknameModifyDto dto);

    @Mapping(source = "userIdentifier", target = "providerId")
    UserSignUpCommand toCommand(UserSignUpDto userSignUpDto);
}
