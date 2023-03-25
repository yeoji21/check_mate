package checkmate.user.application.dto;

import checkmate.user.application.dto.request.SignUpCommand;
import checkmate.user.application.dto.request.UserSignUpCommand;
import checkmate.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserCommandMapper {
    UserCommandMapper INSTANCE = Mappers.getMapper(UserCommandMapper.class);

    User toEntity(SignUpCommand command);

    User toEntity(UserSignUpCommand command);
}
