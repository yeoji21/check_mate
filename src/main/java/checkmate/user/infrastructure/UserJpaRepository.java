package checkmate.user.infrastructure;

import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class UserJpaRepository implements UserRepository {
    private final UserDao userDao;

    @Override
    public Optional<User> findById(long userId) {
        return userDao.findById(userId);
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        return userDao.findByNickname(nickname);
    }

    @Override
    public Optional<User> findByProviderId(String providerId) {
        return userDao.findByProviderId(providerId);
    }

    @Override
    public Optional<String> findNicknameById(long userId) {
        // TODO: 2023/02/11
        return null;
    }

    @Override
    public User save(User user) {
        return userDao.save(user);
    }
}
