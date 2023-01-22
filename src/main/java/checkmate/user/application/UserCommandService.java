package checkmate.user.application;

import checkmate.exception.code.ErrorCode;
import checkmate.exception.NotFoundException;
import checkmate.user.application.dto.UserCommandMapper;
import checkmate.user.application.dto.request.UserNicknameModifyCommand;
import checkmate.user.application.dto.request.UserSignUpCommand;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserCommandService {
    private final UserRepository userRepository;
    private final UserCommandMapper userCommandMapper;

    @Transactional
    public void signUp(UserSignUpCommand command) {
        User user = userCommandMapper.toEntity(command);
        userRepository.save(user);
    }

    @Transactional
    public void nicknameUpdate(UserNicknameModifyCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, command.userId()));
        user.changeNickname(command.nickname());
    }
}
