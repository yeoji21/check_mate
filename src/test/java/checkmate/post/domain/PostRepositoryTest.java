package checkmate.post.domain;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import checkmate.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostRepositoryTest extends RepositoryTest {
    @Test
    @DisplayName("게시글과 좋아요 목록 조회 - postId")
    void findWithLikes() throws Exception {
        //given
        Mate mate = createMate();
        Post post = TestEntityFactory.post(mate);
        em.persist(post);
        for (int i = 0; i < 5; i++) {
            post.addLikes(i);
        }

        //when
        Post findPost = postRepository.findWithLikes(post.getId()).orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(post).isEqualTo(findPost);
        assertThat(findPost.getLikes()).hasSize(5);
    }

    private Mate createMate() {
        Goal goal = TestEntityFactory.goal(null, "title");
        em.persist(goal);
        User user = TestEntityFactory.user(null, "user");
        em.persist(user);
        Mate mate = goal.join(user);
        em.persist(mate);
        return mate;
    }
}