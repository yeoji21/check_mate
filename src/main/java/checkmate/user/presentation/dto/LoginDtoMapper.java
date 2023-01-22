package checkmate.user.presentation.dto;

import checkmate.user.application.dto.request.SnsLoginCommand;
import checkmate.user.application.dto.request.TokenReissueCommand;
import checkmate.user.domain.ProviderIdGenerator;
import checkmate.user.presentation.dto.request.GoogleLoginDto;
import checkmate.user.presentation.dto.request.KakaoLoginDto;
import checkmate.user.presentation.dto.request.NaverLoginDto;
import checkmate.user.presentation.dto.request.TokenReissueDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LoginDtoMapper {
    LoginDtoMapper INSTANCE = Mappers.getMapper(LoginDtoMapper.class);

    @Mapping(target = "providerId", source = "providerId", qualifiedByName = "kakaoId")
    SnsLoginCommand toCommand(KakaoLoginDto kakaoLoginDto);

    @Named("kakaoId")
    default String kakaoId(String providerId) {
        return ProviderIdGenerator.kakao(providerId);
    }

    @Mapping(target = "providerId", source = "providerId", qualifiedByName = "googleId")
    SnsLoginCommand toCommand(GoogleLoginDto googleLoginDto);

    @Named("googleId")
    default String googleId(String providerId) {
        return ProviderIdGenerator.google(providerId);
    }

    @Mapping(target = "providerId", source = "providerId", qualifiedByName = "naverId")
    SnsLoginCommand toCommand(NaverLoginDto naverLoginDto);

    @Named("naverId")
    default String naverId(String providerId) {
        return ProviderIdGenerator.naver(providerId);
    }

    TokenReissueCommand toCommand(TokenReissueDto tokenReissueDto);
}
