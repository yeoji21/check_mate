package checkmate.user.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends RepositoryTest {
    @Test
    @DisplayName("id로 조회")
    void findById() throws Exception {
        //given
        User user = createUser();
        em.flush();
        em.clear();

        //when
        User foundUser = userRepository.findById(user.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(foundUser).isEqualTo(user);
    }

    @Test
    @DisplayName("닉네임으로 조회")
    void findByNickname() throws Exception {
        //given
        User user = createUser();
        em.flush();
        em.clear();

        //when
        User foundUser = userRepository.findByNickname(user.getNickname())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(foundUser).isEqualTo(user);
    }

    @Test
    @DisplayName("id로 유저의 닉네임 조회")
    void findNicknameById() throws Exception {
        //given
        User user = createUser();
        em.flush();
        em.clear();

        //when
        String nickname = userRepository.findNicknameById(user.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(nickname).isEqualTo(user.getNickname());
    }

    @Test
    @DisplayName("저장")
    void save() throws Exception {
        //given
        User user = TestEntityFactory.user(null, "user");

        //when
        userRepository.save(user);

        //then
        assertThat(user.getId()).isGreaterThan(0L);
    }

    private User createUser() {
        User user = TestEntityFactory.user(null, "user");
        em.persist(user);
        return user;
    }
}