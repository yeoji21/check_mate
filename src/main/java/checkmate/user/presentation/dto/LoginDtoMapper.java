package checkmate.user.presentation.dto;

import checkmate.user.application.dto.request.LoginCommand;
import checkmate.user.application.dto.request.TokenReissueCommand;
import checkmate.user.presentation.dto.request.LoginRequestDto;
import checkmate.user.presentation.dto.request.TokenReissueDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LoginDtoMapper {
    LoginDtoMapper INSTANCE = Mappers.getMapper(LoginDtoMapper.class);

    LoginCommand toCommand(LoginRequestDto loginRequestDto);

    TokenReissueCommand toCommand(TokenReissueDto tokenReissueDto);
}
