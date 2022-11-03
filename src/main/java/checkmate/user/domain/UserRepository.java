package checkmate.user.domain;

import java.util.Optional;

public interface UserRepository{
    Optional<User> findById(long userId);
    Optional<User> findByNickname(String nickname);
    Optional<User> findByProviderId(String providerId);
    User save(User user);
}
