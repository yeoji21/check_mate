package checkmate.user.application;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.user.infrastructure.UserQueryDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserQueryService {
    private final UserQueryDao userQueryDao;

    @Transactional(readOnly = true)
    public void existsNicknameCheck(String nickname) {
        if (userQueryDao.isExistsNickname(nickname))
            throw new BusinessException(ErrorCode.DUPLICATED_NICKNAME);
    }
}
