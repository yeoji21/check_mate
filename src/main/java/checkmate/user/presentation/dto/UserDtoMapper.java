package checkmate.user.presentation.dto;

import checkmate.user.application.dto.request.UserNicknameModifyCommand;
import checkmate.user.application.dto.request.UserSignUpCommand;
import checkmate.user.presentation.dto.request.UserNicknameModifyDto;
import checkmate.user.presentation.dto.request.UserSignUpDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {
    UserDtoMapper INSTANCE = Mappers.getMapper(UserDtoMapper.class);

    @Mapping(source = "dto.nickname", target = "nickname")
    UserNicknameModifyCommand toCommand(long userId, UserNicknameModifyDto dto);

    UserSignUpCommand toCommand(UserSignUpDto userSignUpDto);
}
