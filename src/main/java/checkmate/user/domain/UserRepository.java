package checkmate.user.domain;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(long userId);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByProviderId(String providerId);

    Optional<String> findNicknameById(long userId);

    int countOngoingGoals(long userId);

    User save(User user);

    void delete(User user);
}
