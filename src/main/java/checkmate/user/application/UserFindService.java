package checkmate.user.application;

import checkmate.exception.BusinessException;
import checkmate.exception.ErrorCode;
import checkmate.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserFindService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public void existsNicknameCheck(String nickname) {
        if (userRepository.findByNickname(nickname).isPresent())
            throw new BusinessException(ErrorCode.DUPLICATED_NICKNAME);
    }
}
