package checkmate.user.presentation.dto;

import checkmate.config.auth.JwtUserDetails;
import checkmate.user.application.dto.request.SnsLoginCommand;
import checkmate.user.application.dto.request.UserNicknameModifyCommand;
import checkmate.user.application.dto.request.UserSignUpCommand;
import checkmate.user.domain.ProviderIdGenerator;
import checkmate.user.presentation.dto.request.GoogleSignUpDto;
import checkmate.user.presentation.dto.request.KakaoSignUpDto;
import checkmate.user.presentation.dto.request.NaverSignUpDto;
import checkmate.user.presentation.dto.request.UserNicknameModifyDto;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {
    UserDtoMapper userDtoMapper = Mappers.getMapper(UserDtoMapper.class);

    @Mapping(target = "providerId", source = "providerId", qualifiedByName = "kakaoId")
    UserSignUpCommand toCommand(KakaoSignUpDto kakaoSignUpDto);

    @Mapping(target = "providerId", source = "providerId", qualifiedByName = "kakaoId")
    SnsLoginCommand toLoginCommand(KakaoSignUpDto kakaoSignUpDto);

    @Named("kakaoId")
    default String kakaoId(String providerId) {
        return ProviderIdGenerator.kakao(providerId);
    }

    @Mapping(target = "providerId", source = "providerId", qualifiedByName = "googleId")
    UserSignUpCommand toCommand(GoogleSignUpDto googleSignUpDto);

    @Mapping(target = "providerId", source = "providerId", qualifiedByName = "googleId")
    SnsLoginCommand toLoginCommand(GoogleSignUpDto googleSignUpDto);

    @Named("googleId")
    default String googleId(String providerId) {
        return ProviderIdGenerator.google(providerId);
    }

    @Mapping(target = "providerId", source = "providerId", qualifiedByName = "naverId")
    UserSignUpCommand toCommand(NaverSignUpDto naverSignUpDto);

    @Mapping(target = "providerId", source = "providerId", qualifiedByName = "naverId")
    SnsLoginCommand toLoginCommand(NaverSignUpDto naverSignUpDto);

    @Named("naverId")
    default String naverId(String providerId) {
        return ProviderIdGenerator.naver(providerId);
    }

    @Mappings({
            @Mapping(source = "userDetails", target = "userId", qualifiedByName = "userId"),
            @Mapping(source = "dto.nickname", target = "nickname")
    })
    UserNicknameModifyCommand toCommand(JwtUserDetails userDetails, UserNicknameModifyDto dto);

    @Named("userId")
    default Long getUserId(JwtUserDetails userDetails) {
        return userDetails.getUserId();
    }
}
