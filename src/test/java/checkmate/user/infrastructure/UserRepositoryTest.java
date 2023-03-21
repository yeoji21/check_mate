package checkmate.user.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends RepositoryTest {
    @Test
    void findById() throws Exception {
        //given
        User user = TestEntityFactory.user(null, "user");
        em.persist(user);
        em.flush();
        em.clear();

        //when
        User foundUser = userRepository.findById(user.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(foundUser.getId()).isEqualTo(user.getId());
    }

    @Test
    void findByNickname() throws Exception {
        //given
        User user = TestEntityFactory.user(null, "user");
        em.persist(user);
        em.flush();
        em.clear();

        //when
        User foundUser = userRepository.findByNickname(user.getNickname())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(foundUser.getId()).isEqualTo(user.getId());
    }

    @Test
    void findByProviderId() throws Exception {
        //given
        User user = TestEntityFactory.user(null, "user");
        em.persist(user);
        em.flush();
        em.clear();

        //when
        User foundUser = userRepository.findByProviderId(user.getProviderId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(foundUser.getId()).isEqualTo(user.getId());
    }

    @Test
    void findNicknameById() throws Exception {
        //given
        User user = TestEntityFactory.user(null, "user");
        em.persist(user);
        em.flush();
        em.clear();

        //when
        String nickname = userRepository.findNicknameById(user.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(nickname).isEqualTo(user.getNickname());
    }

    @Test
    void save() throws Exception {
        //given
        User user = TestEntityFactory.user(null, "user");

        //when
        userRepository.save(user);

        //then
        assertThat(user.getId()).isGreaterThan(0L);
    }
}